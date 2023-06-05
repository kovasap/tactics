(ns app.interface.gridmap
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [perlin2d.core :as p]
            [app.interface.lands :refer [lands]]
            [app.interface.utils :refer [get-only]]
            [clojure.string :as st]))

; TODO use one perlin noise for humidity and one for elevation to generate more
; land types in interesting ways

(defn get-land-below-perlin-cutoff
  [perlin-cutoff]
  (first (first (filter (fn [[_ nxt]] (> (:perlin-cutoff nxt) perlin-cutoff))
                  (partition 2 1 (sort-by :perlin-cutoff lands))))))

(assert (= (:type (get-land-below-perlin-cutoff 0.5)) :forest))


; For a 12x12 map octaves freq amp of 1 0.08 2 seems to work well

(defn get-perlin-land
  [row-idx col-idx]
  (get-land-below-perlin-cutoff
    (let [octaves   8 ; The number of times the algorithm will run. A higher
                      ; number should result in more random looking results.
          frequency 0.05 ; should be from 0.0-1.0?
          amplitude 1]
      ; We add amplitude and divide by 2 to make sure all values are between 0
      ; and amplitude.
      (/ (+ amplitude
            (p/do-octave col-idx row-idx octaves frequency amplitude))
         2))))

(defn base-tile
  [args]
  (merge {:row-idx          nil
          :col-idx          nil
          :land             nil}
         args))

(defn tile-from-str
  [row-idx col-idx [tile-letter bonus-letter]]
  (base-tile {:row-idx row-idx
              :col-idx col-idx
              :land    (get-only lands :letter tile-letter)}))

(defn parse-gridmap-str
  "Returns 2d array of tile maps."
  [board-str]
  (into []
        (map-indexed
          (fn [row-idx line]
            (into []
                  (map-indexed (fn [col-idx tile-str]
                                 (tile-from-str row-idx col-idx tile-str))
                               (st/split (st/trim line) #" +"))))
          (st/split-lines board-str))))

(def manual-gridmap
  (parse-gridmap-str
    "F   M   M   F   F   P   P   W  
     W   W   M   F   F   P   P   W
     W   M   F   F   F   P   P   W
     W   M   M   F   F   P   P   W
     W   M   M   F   F   F   P   W
     W   M   M   F   F   F   F   W
     W   M   M   F   W   F   F   W
     W   M   M   W   W   W   F   W
     S   M   S   S   W   F   F   W
     S   S   S   S   S   F   F   W
     S   S   S   S   S   F   F   W"))


(defn generate-perlin-board
  "Returns 2d array of tile maps."
  [width height]
  (into []
        (for [row-idx (range height)]
          (into []
                (for [col-idx (range width)]
                  (base-tile {:row-idx row-idx
                              :col-idx col-idx
                              :land    (get-perlin-land row-idx col-idx)}))))))
