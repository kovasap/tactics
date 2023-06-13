(ns app.interface.intentions
  (:require [re-frame.core :as rf]
            [app.interface.movement
             :refer
             [get-path
              declare-move-intention
              truncate-path
              get-tiles-left-to-move
              reset-movement-status
              execute-movements]]
            [app.interface.attacking
             :refer
             [get-attacks tile-in-attack-range?]]
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
 [character characters-by-full-name gridmap]
 ((partial declare-move-intention
           character
           (truncate-occupied-path-steps
             (truncate-path (get-path-to-nearest-player-character
                                   gridmap
                                   character
                                   characters-by-full-name)
                            (get-tiles-left-to-move character))))
  gridmap))

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
  (first
    (for [{:keys [intention-character-full-name]}
          (get-tiles
            gridmap
            (partial tile-in-attack-range?
                     attacker
                     (or (get-characters-current-intention-tile gridmap
                                                                attacker)
                         (get-characters-current-tile gridmap attacker))
                     gridmap))
          :when (not (nil? intention-character-full-name))]
      intention-character-full-name)))

(defn get-attack-intentions
  [gridmap characters-by-full-name]
  (reduce concat
    (for [attacker (vals characters-by-full-name)
          :let     [target-full-name (get-attack-target-full-name gridmap attacker)]
          :when    (and (not (nil? target-full-name))
                        (not (:controlled-by-player? attacker)))]
      (get-attacks attacker (characters-by-full-name target-full-name)))))

(defn update-opponent-intentions
  ([db _] (update-opponent-intentions db))
  ([{:keys [current-scene-idx characters] :as db}]
   (->
     db
     (assoc :opponent-intentions-updated true)
     (update-in [:scenes current-scene-idx :gridmap]
                (partial update-move-intentions characters))
     (update :pending-attacks
             #(into []
               (concat %
                       (get-attack-intentions
                              (get-in db [:scenes current-scene-idx :gridmap])
                              characters)))))))

(rf/reg-event-db
  :update-opponent-intentions
  update-opponent-intentions)

(rf/reg-event-fx
  :execute-intentions
  (fn [{{:keys [current-scene-idx opponent-intentions-updated] :as db} :db} _]
    {:db
      (-> db
          (#(if (not opponent-intentions-updated)
             (update-opponent-intentions %)
             db))
          (update :characters reset-movement-status)
          (update-in [:scenes current-scene-idx :gridmap] execute-movements))
     :fx [[:dispatch [:execute-attacks]]]}))
