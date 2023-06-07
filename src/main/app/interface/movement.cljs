(ns app.interface.movement
  (:require [re-frame.core :as rf]
            [day8.re-frame.undo :as undo :refer [undoable]]  
            [astar.core :refer [route]]
            [app.interface.gridmap :refer [get-tiles update-tiles get-current-tile get-adjacent-tiles]]))

; See this for a way to find paths as well!
; https://noobtuts.com/clojure/manhattan-distance-and-path
(defn distance
  [[x1 y1] [x2 y2]]
  (+ (abs (- x1 x2))
     (abs (- y1 y2))))

(defn get-steps-to-move-to
  [{{:keys [steps-to-move-through]} :land}]
  (if (nil? steps-to-move-through)
     100 ; technically infinity, this is impassible
     steps-to-move-through))

(defn get-path-steps
  [path]
  (reduce + (for [tile (rest path)] (get-steps-to-move-to tile))))

(defn gridmap->astar-args
  [gridmap]
  {:h     (into {} (for [tile (get-tiles gridmap)] [tile 0]))
   :graph (into {}
                (for [tile (get-tiles gridmap)]
                  [tile (get-adjacent-tiles gridmap tile)]))
   :dist  (fn [_ to-tile] (get-steps-to-move-to to-tile))})

; TODO memoize this
(defn get-path
  "Returns list of tiles in visited order."
  [gridmap start-tile end-tile]
  (let [{:keys [graph h dist]} (gridmap->astar-args gridmap)]
    (vec (conj (route graph dist h start-tile end-tile)
               start-tile))))

(def get-path-m (memoize get-path))
  

(defn get-tiles-left-to-move
  [{:keys [tiles-already-moved] {:keys [air]} :affinities}]
  (- air tiles-already-moved))

(defn begin-move
  [character gridmap]
  (let [{from-row-idx :row-idx from-col-idx :col-idx :as from-tile}
        (get-current-tile gridmap character)]
    (update-tiles gridmap
                  (fn [tile]
                    (> (inc (get-tiles-left-to-move character))
                       (get-path-steps (get-path gridmap from-tile tile))))
                  #_(fn [{:keys [row-idx col-idx]}]
                      (> (inc (get-tiles-left-to-move character))
                         (distance [from-row-idx from-col-idx]
                                   [row-idx col-idx])))
                  #(assoc % :is-legal-move true))))

(rf/reg-event-db
  :begin-move
  (undoable "Begin Move")
  (fn [{:keys [current-scene-idx] :as db} [_ character]]
    (-> db
      (update-in [:scenes current-scene-idx :gridmap]
                 (partial begin-move character))
      (assoc :moving-character character))))

(defn clear-legal-moves
  [gridmap]
  (update-tiles gridmap #(dissoc % :is-legal-move)))

(rf/reg-event-db
  :cancel-move
  (fn [{:keys [current-scene-idx] :as db} _]
    (-> db
      (update-in [:scenes current-scene-idx :gridmap] clear-legal-moves)
      (dissoc :moving-character))))
   

(defn declare-move-intention
  [{:keys [full-name]}
   path
   gridmap]
  (let [{from-row-idx :row-idx from-col-idx :col-idx :as from-tile} (first path)
        steps (subvec path 1 (dec (count path)))
        {to-row-idx :row-idx to-col-idx :col-idx :as to-tile} (last path)]
    (-> gridmap
        (clear-legal-moves)
        ; Add waypoints
        ((apply comp
           (for [{path-row-idx :row-idx path-col-idx :col-idx} steps]
             #(assoc-in % [path-row-idx path-col-idx :waypoint] true))))
        (assoc-in [from-row-idx from-col-idx :intention-character-full-name]
                  nil)
        (assoc-in [to-row-idx to-col-idx :intention-character-full-name]
                  full-name))))

(rf/reg-event-fx
  :declare-move-intention
  (undoable "Declare move intention")
  (fn [cofx [_ end-tile]]
    {:db (let [{:keys [current-scene-idx moving-character] :as db} (:db cofx)
               gridmap      (get-in db [:scenes current-scene-idx :gridmap])
               start-tile   (get-current-tile gridmap moving-character)
               path (get-path-m gridmap start-tile end-tile)]
           (-> db
               (update-in [:scenes current-scene-idx :gridmap]
                          (partial declare-move-intention
                                   moving-character
                                   path))
               (assoc-in [:characters
                          (:full-name moving-character)
                          :tiles-already-moved]
                         (dec (count path)))
               (dissoc :moving-character)))
     :fx [[:dispatch [:update-intentions]]]}))
