(ns app.interface.intentions
  (:require [re-frame.core :as rf]
            [app.interface.pathfinding
             :refer
             [get-usable-path-to-nearest-player-character
              get-usable-path-to-nearest-attackable-player-character
              get-path-usable-by-character]]
            [app.interface.movement :refer [declare-move-intention]]
            [app.interface.attacking
             :refer
             [get-attacks tile-in-attack-range? get-post-attacks-character]]
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
    declare-move-intention
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
  (reduce concat
    (for [attacker (vals characters-by-full-name)
          :let     [target-full-name (get-attack-target-full-name gridmap attacker)]
          :when    (and (not (nil? target-full-name))
                        (not (:dead attacker))
                        (not (:controlled-by-player? attacker)))]
      (get-attacks attacker (characters-by-full-name target-full-name)))))


(rf/reg-event-db
  :update-opponent-move-intentions
  (fn [{:keys [current-scene-idx characters] :as db}]
     (update-in db [:scenes current-scene-idx :gridmap]
                (partial update-move-intentions characters))))

(rf/reg-event-db
  :update-opponent-attack-intentions
  (fn [{:keys [current-scene-idx characters] :as db}]
    (update db
            :intended-attacks
            #(into []
                   (concat %
                           (get-attack-intentions
                             (get-in db [:scenes current-scene-idx :gridmap])
                             characters))))))

(rf/reg-event-fx
  :update-opponent-intentions
  (fn [{:keys [db]} _]
    {:fx [[:dispatch [:update-opponent-move-intentions]]
          [:dispatch [:update-opponent-attack-intentions]]]}))

(rf/reg-event-fx
  :execute-intentions
  (fn [{:keys [db]} _]
    {:fx [[:dispatch [:update-opponent-intentions]]
          [:dispatch [:execute-intended-movements]]
          [:dispatch [:execute-intended-attacks]]
          [:dispatch [:update-opponent-intentions]]]}))

; Get a version of this character as they will look on the next turn.
(rf/reg-sub
  :next-turn-character
  (fn [{:keys [intended-attacks]} [_ character]]
    (-> character
        (get-post-attacks-character intended-attacks))))
