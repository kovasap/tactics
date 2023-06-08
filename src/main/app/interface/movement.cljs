(ns app.interface.movement
  (:require [re-frame.core :as rf]
            [day8.re-frame.undo :as undo :refer [undoable]]  
            [astar.core :refer [route]]
            [app.interface.gridmap :refer [get-tiles update-tiles get-characters-current-tile get-adjacent-tiles]]))

(defn get-steps-to-move-to
  [{{:keys [steps-to-move-through]} :land
    :keys [character-full-name]}]
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
  

(defn get-tiles-left-to-move
  [{:keys [tiles-already-moved] {:keys [air]} :affinities}]
  (- air tiles-already-moved))

(defn begin-move
 [character gridmap]
 (let [from-tile (get-characters-current-tile gridmap character)]
  (update-tiles
   gridmap
   (fn [tile]
    (> (inc (get-tiles-left-to-move character))
       ; alternative: as the bird flies distance
       ; (distance from-tile tile)
       (get-number-of-path-steps (get-path gridmap from-tile tile))))
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
 [{:keys [full-name]} path gridmap]
 (let [{from-row-idx :row-idx from-col-idx :col-idx} (first path)
       steps (subvec path 1 (dec (count path)))
       {to-row-idx :row-idx to-col-idx :col-idx} (last path)]
  (-> gridmap
      (clear-legal-moves)
      ; Add waypoints
      ((apply comp
        (for [{path-row-idx :row-idx path-col-idx :col-idx} steps]
         #(assoc-in % [path-row-idx path-col-idx :waypoint-for] full-name))))
      (assoc-in [from-row-idx from-col-idx :intention-character-full-name] nil)
      (assoc-in [to-row-idx to-col-idx :intention-character-full-name]
                full-name))))

(rf/reg-event-fx
  :declare-move-intention
  (undoable "Declare move intention")
  (fn [cofx [_ end-tile]]
    {:db (let [{:keys [current-scene-idx moving-character] :as db} (:db cofx)
               gridmap      (get-in db [:scenes current-scene-idx :gridmap])
               start-tile   (get-characters-current-tile gridmap moving-character)
               path (get-path gridmap start-tile end-tile)]
           (-> db
               (update-in [:scenes current-scene-idx :gridmap]
                          (partial declare-move-intention
                                   moving-character
                                   path))
               (assoc-in [:characters
                          (:full-name moving-character)
                          :tiles-already-moved]
                         (dec (count path)))
               (assoc-in [:characters
                          (:full-name moving-character)
                          :has-intention?]
                         true)
               (dissoc :moving-character)))
     :fx [[:dispatch [:update-intentions]]]}))

(rf/reg-sub
  :moving-character
  (fn [db _]
    (:moving-character db)))
