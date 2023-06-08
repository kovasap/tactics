(ns app.interface.intentions
 (:require
  [re-frame.core :as rf]
  [app.interface.movement
   :refer
   [get-path declare-move-intention truncate-path get-tiles-left-to-move]]
  [app.interface.gridmap
   :refer
   [get-characters-current-tile update-tiles get-tiles]]))

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

(defn update-intention
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

(defn update-intentions
 [characters-by-full-name gridmap]
 ((apply comp
   (for [character (vals characters-by-full-name)
         :when     (not (:controlled-by-player? character))]
    (partial update-intention character characters-by-full-name)))
  gridmap))


(rf/reg-event-db
  :update-intentions
  (fn [{:keys [current-scene-idx characters] :as db} _]
    (update-in db [:scenes current-scene-idx :gridmap]
                  (partial update-intentions characters))))


(defn get-moved-character-full-names
  [gridmap]
  (into #{} (map :intention-character-full-name
              (get-tiles gridmap :intention-character-full-name))))


(def character-moved?
 (memoize (fn [gridmap {:keys [character-full-name]}]
           ((get-moved-character-full-names gridmap) character-full-name))))

(defn commit-movements
  [gridmap]
  (-> gridmap
      ; remove old positons for moved characters
      (update-tiles (partial character-moved? gridmap)
                    (fn [tile] (dissoc tile :character-full-name)))
      ; remove waypoints
      ; TODO if hitting a waypoints triggers any effect, do it here
      (update-tiles (fn [{:keys [waypoint-for]}] waypoint-for)
                    (fn [tile] (dissoc tile :waypoint-for)))
      ; add new positions
      (update-tiles
        :intention-character-full-name
        (fn [{:keys [intention-character-full-name] :as tile}]
          (-> tile
              (assoc :character-full-name
                     intention-character-full-name)
              (dissoc :intention-character-full-name))))))

(defn reset-movement-status
  [characters]
  (into {}
        (for [[full-name character] characters]
          [full-name (assoc character :tiles-already-moved 0
                                      :has-intention? false)])))

; TODO make it so that all the :under-attack-by characters are attacked by
; their attackers
(defn commit-attacks
  [characters]
  characters)

(rf/reg-event-db
  :commit-intentions
  (fn [{:keys [current-scene-idx] :as db} _]
    (->
      db
      (update :characters reset-movement-status)
      (update-in [:scenes current-scene-idx :gridmap] commit-movements)
      (update :characters commit-attacks))))
