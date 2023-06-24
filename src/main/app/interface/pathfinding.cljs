(ns app.interface.pathfinding
  (:require [astar.core :refer [route]]
            [app.interface.gridmap
             :refer
             [get-tiles
              has-targetable-player-character?
              get-characters-current-tile
              get-adjacent-tiles
              get-characters-current-intention-tile]]
            [app.interface.character-stats :refer [get-tiles-left-to-move]]
            [app.interface.attacking :refer [tile-in-attack-range?]]))

(defn get-steps-to-move-to
  [{{:keys [steps-to-move-through]} :land
    :keys [character-full-name] :as tile}]
  (cond
    (nil? steps-to-move-through) 100 ; technically infinity, this is impassible
    character-full-name 100 ; cannot move to a tile with a character!
    :else steps-to-move-through))

(defn get-number-of-path-steps
  [path]
  (reduce + (for [tile (rest path)] (get-steps-to-move-to tile))))

(defn gridmap->astar-args
  [gridmap]
  {:h     (into {} (for [tile (get-tiles gridmap)] [tile 0]))
   :graph (into {}
                (for [tile (get-tiles gridmap)]
                  [tile (get-adjacent-tiles gridmap tile)]))
   :dist  (fn [_ to-tile] (get-steps-to-move-to to-tile))})


(def get-path
 "Returns list of tiles in visited order."
 (memoize
  (fn [gridmap start-tile end-tile]
   (let [{:keys [graph h dist]} (gridmap->astar-args gridmap)]
    (vec (conj (route graph dist h start-tile end-tile) start-tile))))))


(defn truncate-path
  "Takes away tiles from the end of the path until it is under steps."
  [path steps]
  (if (>= steps (get-number-of-path-steps path))
    (vec path)
    (truncate-path (butlast path) steps))) 

(defn truncate-occupied-path-steps
  [path]
  (if (:intention-character-full-name (last path))
    (truncate-occupied-path-steps (butlast path))
    (vec path)))

(defn get-path-usable-by-character
  "Truncate the given path so that the given character can actually take it on
  turn end."
  [path character]
  (truncate-occupied-path-steps
    (truncate-path path (get-tiles-left-to-move character))))

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
              (or (get-characters-current-intention-tile gridmap
                                                         character)
                  (get-characters-current-tile gridmap character))
              player-character-tile)))))

(defn get-usable-path-to-nearest-player-character
  [gridmap character characters-by-full-name]
  (get-path-usable-by-character
    (get-path-to-nearest-player-character gridmap
                                          character
                                          characters-by-full-name)
    character))

(defn get-usable-path-to-nearest-attackable-player-character
  [gridmap character characters-by-full-name]
  (let [candidate-path (get-usable-path-to-nearest-player-character
                         gridmap
                         character
                         characters-by-full-name)
        final-location (last candidate-path)]
    (if (empty?
          (get-tiles gridmap
                     #(and (tile-in-attack-range? character final-location %)
                           (has-targetable-player-character?
                             %
                             characters-by-full-name))))
      []
      candidate-path)))
