(ns app.interface.intentions
  (:require
    [re-frame.core :as rf]
    [app.interface.movement
     :refer
     [get-path
      declare-move-intention
      truncate-path
      get-tiles-left-to-move
      reset-movement-status
      commit-movements]]
    [app.interface.attacking :refer [commit-attacks tile-in-attack-range?]]
    [app.interface.gridmap
     :refer
     [get-adjacent-tiles
      get-characters-current-tile
      get-characters-current-intention-tile
      update-tiles
      get-tiles]]))

; TODO add "aggresive" "cautious" and other "personalities" to the AI movement
; potentially depending on their element affinity.

(defn get-path-to-nearest-player-character
 [gridmap character characters-by-full-name]
 (first
  (sort-by
   count
   (for [player-character-tile (get-tiles gridmap
                                          (fn [{:keys [character-full-name]}]
                                           (:controlled-by-player?
                                            (characters-by-full-name
                                             character-full-name))))]
    (get-path gridmap
              (get-characters-current-tile gridmap character)
              player-character-tile)))))

(defn truncate-occupied-path-steps
  [path]
  (if (:intention-character-full-name (last path))
    (truncate-occupied-path-steps (butlast path))
    (vec path)))

(defn update-move-intention
 "Returns a gridmap with :intention-character-full-name tile keys filled in."
 [{:keys [full-name] :as character} characters-by-full-name gridmap]
 (-> gridmap
     ; First clear old intentions
     ; TODO Note that we may want to do this for all characters not controlled
     ; by the player upfront to avoid cases where characters avoid spaces
     ; occupied by old intentions
     (update-tiles #(= full-name (:intention-character-full-name %))
                   #(dissoc % :intention-character-full-name))
     ; Clear old waypoints
     (update-tiles #(= full-name (:waypoint-for %)) #(dissoc % :waypoint-for))
     ; Make AI movement intentions.
     ((partial declare-move-intention
               character
               (truncate-occupied-path-steps
                 (truncate-path (get-path-to-nearest-player-character
                                       gridmap
                                       character
                                       characters-by-full-name)
                                (get-tiles-left-to-move character)))))))

(defn update-move-intentions
  [characters-by-full-name gridmap]
  ((apply comp
    (for [character (vals characters-by-full-name)
          :when     (not (:controlled-by-player? character))]
     (partial update-move-intention character characters-by-full-name)))
   gridmap))

(defn get-attack-target-full-name
  [gridmap attacker]
  ; TODO maybe make more intelligent target selection?
  #p
   (first (for [{:keys [intention-character-full-name character-full-name]}
                (get-tiles
                  gridmap
                  (partial tile-in-attack-range?
                           attacker
                           (or (get-characters-current-intention-tile gridmap
                                                                      attacker)
                               (get-characters-current-tile gridmap attacker))
                           gridmap))
                :let  [target (or intention-character-full-name
                                  character-full-name)]
                :when (not (nil? target))]
            target)))

(defn update-attack-intentions
  "Mark all player characters :under-attack-by adjacent enemies."
  [gridmap characters-by-full-name]
  ((apply comp
    (for [attacker (vals characters-by-full-name)
          :let     [target (get-attack-target-full-name gridmap attacker)]
          :when    (and (not (nil? target))
                        (not (:controlled-by-player? attacker)))]
     (fn [characters]
      (update-in characters [target :under-attack-by] #(into [attacker] %)))))
   characters-by-full-name))

(rf/reg-event-db
  :update-intentions
  (fn [{:keys [current-scene-idx characters] :as   db} _]
    (-> db
        (update-in [:scenes current-scene-idx :gridmap]
                   (partial update-move-intentions characters))
        (update :characters
                (partial update-attack-intentions
                         (get-in db [:scenes current-scene-idx :gridmap]))))))

(rf/reg-event-db
  :commit-intentions
  (fn [{:keys [current-scene-idx] :as db} _]
    (-> db
        (update :characters reset-movement-status)
        (update-in [:scenes current-scene-idx :gridmap] commit-movements)
        (update :characters commit-attacks))))
