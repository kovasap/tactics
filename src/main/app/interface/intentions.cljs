(ns app.interface.intentions
  (:require
    [re-frame.core :as rf]
    [app.interface.pathfinding
     :refer
     [get-usable-path-to-nearest-player-character
      get-usable-path-to-nearest-attackable-player-character
      get-path-usable-by-character]]
    [app.interface.movement :refer [make-move-intention]]
    [app.interface.character-stats :refer [get-speed]]
    [app.interface.re-frame-utils :refer [dispatch-sequentially-with-timings]]
    [app.interface.attacking
     :refer
     [get-attacks
      tile-in-attack-range?
      get-post-attacks-character
      get-duration-of-attacks-ms
      filter-dead-attacks]]
    [app.interface.gridmap
     :refer
     [get-characters-current-tile
      get-characters-current-intention-tile
      get-tiles]]))

; TODO add "aggresive" "cautious" and other "personalities" to the AI movement
; potentially depending on their element affinity.

; Map of keywords to functions that take in a
; [gridmap character characters-by-full-name] and return a path the AI
; character will take.
(def ai-behaviors
  {; Always run at the nearest player character and try to attack, no matter
   ; how far away they are.
   :aggressive      get-usable-path-to-nearest-player-character
   ; Only move when an attack is possible from the newly moved to location.
   :attack-in-range get-usable-path-to-nearest-attackable-player-character})

(defn update-move-intention
  "Returns a gridmap with :intention-character-full-name tile keys filled in."
  [{:keys [ai-behavior] :as character} characters-by-full-name gridmap]
  ((partial
    make-move-intention
    character
    ((ai-behavior ai-behaviors) gridmap character characters-by-full-name))
   gridmap))

(defn update-move-intentions
  [characters-by-full-name gridmap]
  ((apply comp
    (for [character (vals characters-by-full-name)
          :when     (and (not (:dead character))
                         (not (:controlled-by-player? character)))]
     (partial update-move-intention character characters-by-full-name)))
   gridmap))

(defn get-attack-target-full-name
  [gridmap attacker]
  ; TODO maybe make more intelligent target selection?
  (first
    (for [{:keys [intention-character-full-name]}
          (get-tiles
            gridmap
            (partial tile-in-attack-range?
                     attacker
                     (or (get-characters-current-intention-tile gridmap
                                                                attacker)
                         (get-characters-current-tile gridmap attacker))))
          :when (not (nil? intention-character-full-name))]
      intention-character-full-name)))

(defn get-attack-intentions
  [gridmap characters-by-full-name]
  (vec
    (reduce concat
      (for [attacker (sort-by get-speed (vals characters-by-full-name))
            :let     [target-full-name (get-attack-target-full-name gridmap
                                                                    attacker)]
            :when    (and (not (nil? target-full-name))
                          (not (:controlled-by-player? attacker)))]
        (get-attacks attacker (characters-by-full-name target-full-name))))))


(rf/reg-event-db
  :update-opponent-move-intentions
  (fn [{:keys [current-scene characters] :as db}]
     (update-in db [:scenes current-scene :gridmap]
                (partial update-move-intentions characters))))

(rf/reg-event-db
  :update-opponent-attack-intentions
  (fn [{:keys [current-scene characters] :as db}]
    (assoc db
      :intended-attacks (filter-dead-attacks
                          (vals characters)
                          (get-attack-intentions (get-in db
                                                         [:scenes
                                                          current-scene
                                                          :gridmap])
                                                 characters)))))

(rf/reg-event-fx
  :update-opponent-intentions
  (fn [{:keys [db]} _]
    {:fx [[:dispatch [:update-opponent-move-intentions]]
          [:dispatch [:update-opponent-attack-intentions]]]}))

(rf/reg-event-fx
  :execute-intentions
  (fn [{:keys [db]} _]
    {:fx (dispatch-sequentially-with-timings
           [[[:update-opponent-intentions] 10]
            [[:execute-intended-movements] 10]
            [[:execute-intended-attacks]
             (get-duration-of-attacks-ms (:intended-attacks db))]
            [[:update-opponent-intentions] 10]])}))

; Get a version of this character as they will look on the next turn.
(rf/reg-sub
  :next-turn-character
  (fn [{:keys [intended-attacks]} [_ character]]
    (-> character
        (get-post-attacks-character intended-attacks))))
