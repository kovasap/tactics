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
  (merge {:row-idx nil
          :col-idx nil
          :land    nil}
         args))

(defn tile-from-str
  [row-idx col-idx [tile-letter bonus-letter] characters-by-letter-code]
  (base-tile
    {:row-idx row-idx
     :col-idx col-idx
     :character-full-name (:full-name (characters-by-letter-code bonus-letter))
     :land    (get-only lands :letter tile-letter)}))

(defn parse-gridmap-str
  "Returns 2d array of tile maps."
  [gridmap-str characters-by-letter-code]
  (into []
        (map-indexed
          (fn [row-idx line]
            (into []
                  (map-indexed (fn [col-idx tile-str]
                                 (tile-from-str row-idx col-idx tile-str
                                                characters-by-letter-code))
                               (st/split (st/trim line) #" +"))))
          (st/split-lines gridmap-str))))

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

(defn update-tiles
  "Applies update-fn to all tiles in the gridmap for which tile-selector
  returns true. Returns a new gridmap."
  ([gridmap update-fn] (update-tiles gridmap any? update-fn))
  ([gridmap tile-selector update-fn]
   (into []
         (for [row gridmap]
           (into []
                 (for [tile row]
                   (if (tile-selector tile) (update-fn tile) tile)))))))

(defn get-tiles
  "Get all tiles for which tile-selector is true."
  ([gridmap] (get-tiles gridmap any?))
  ([gridmap tile-selector]
   (reduce concat
     (for [row gridmap]
       (for [tile row
             :when (tile-selector tile)]
         tile)))))

(defn get-tile
  [gridmap tile-selector]
  (first (get-tiles gridmap tile-selector)))

(defn get-characters-current-tile
  [gridmap {:keys [full-name]}]
  (get-tile gridmap (fn [{:keys [character-full-name]}]
                      (= full-name character-full-name))))

(defn get-adjacent-tiles
  [gridmap {:keys [row-idx col-idx]}]
  (filter #(not (nil? %))
    (for [[row-idx-shift col-idx-shift] [[1 0] [0 1] [-1 0] [0 -1]]]
      (get-in gridmap [(+ row-idx row-idx-shift) (+ col-idx col-idx-shift)]))))
    

(rf/reg-sub
  :current-gridmap
  (fn [db _]
    (:gridmap @(rf/subscribe [:current-scene]))))

(rf/reg-sub
  :characters-current-tile
  (fn [_ [_ full-name]]
   (get-characters-current-tile @(rf/subscribe [:current-gridmap])
                                {:full-name full-name})))
