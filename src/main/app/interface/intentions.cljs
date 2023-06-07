(ns app.interface.intentions 
  (:require
    [re-frame.core :as rf]
    [app.interface.gridmap :refer [update-tiles get-tiles]]))

; TODO make it so that enemy characters move toward the player's characters
(defn calculate-intention
  "Returns a gridmap with :intention-character-full-name tile keys filled in."
  [character gridmap]
  (-> gridmap
    ; First clear old intentions
    ; Note that we may want to do this for all characters not controlled by the
    ; player upfront to avoid cases where characters avoid spaces occupied by
    ; old intentions
    (update-tiles #(= (:full-name character) :intention-character-full-name)
                  #(dissoc % :intention-character-full-name))))
    ; TODO then calculate a new intention!

(defn calculate-intentions
  [characters gridmap]
  ((apply comp
     (for [character characters
           :when     (not (:controlled-by-player? character))]
       (partial calculate-intention character)))
   gridmap))

(rf/reg-event-db
  :update-intentions
  (fn [{:keys [current-scene-idx characters] :as db} _]
    (update-in db [:scenes current-scene-idx :gridmap]
                  (partial calculate-intentions (vals characters)))))


(defn get-moved-character-full-names
  [gridmap]
  (into #{} (map :intention-character-full-name
              (get-tiles gridmap :intention-character-full-name))))

(defn character-moved?
  [gridmap {:keys [character-full-name]}]
  ((get-moved-character-full-names gridmap) character-full-name))

(def character-moved?-m (memoize character-moved?))

(rf/reg-event-db
  :commit-intentions
  (fn [{:keys [current-scene-idx] :as db} _]
    (->
      db
      ; reset movement status
      (update :characters
              (fn [characters]
                (into {}
                      (for [[full-name character] characters]
                        [full-name (assoc character :tiles-already-moved 0
                                                    :has-intention? false)]))))
      ; commit movements
      (update-in
        [:scenes current-scene-idx :gridmap]
        (fn [gridmap]
          (-> gridmap
              ; remove old positons for moved characters
              (update-tiles (partial character-moved?-m gridmap)
                            (fn [tile] (dissoc tile :character-full-name)))
              ; remove waypoints
              ; TODO if hitting a waypoints triggers any effect, do it here
              (update-tiles (fn [{:keys [waypoint]}] waypoint)
                            (fn [tile] (dissoc tile :waypoint)))
              ; add new positions
              (update-tiles
                :intention-character-full-name
                (fn [{:keys [intention-character-full-name] :as tile}]
                  (-> tile
                      (assoc :character-full-name
                             intention-character-full-name)
                      (dissoc :intention-character-full-name))))))))))
