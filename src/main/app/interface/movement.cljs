(ns app.interface.movement
  (:require [re-frame.core :as rf]
            [day8.re-frame.undo :as undo :refer [undoable]]  
            [app.interface.gridmap :refer [update-tiles get-current-tile]]))

; See this for a way to find paths as well!
; https://noobtuts.com/clojure/manhattan-distance-and-path
(defn distance
  [[x1 y1] [x2 y2]]
  (+ (abs (- x1 x2))
     (abs (- y1 y2))))

; TODO memoize this
(defn get-path
  "Returns list of tiles in visited order."
  [gridmap start-tile end-tile]
  [start-tile end-tile])

(defn get-tiles-left-to-move
  [{:keys [tiles-already-moved] {:keys [air]} :affinities}]
  (- air tiles-already-moved))

(defn begin-move
  [character gridmap]
  (let [{cur-row-idx :row-idx cur-col-idx :col-idx} (get-current-tile
                                                      gridmap
                                                      character)]
    ; TODO make this movement work on a per-tile basis, returning a sequence of
    ; tiles visited in order.  This should allow for tiles to effect the
    ; character, and for us to be able to do better animations.
    ; Use a pathfinding algorithm to determine it as
    ; opposed to the raw distance we use here.
    (update-tiles gridmap
                  (fn [{:keys [row-idx col-idx]}]
                    (> (inc (get-tiles-left-to-move character))
                       (distance [cur-row-idx cur-col-idx] [row-idx col-idx])))
                  #(assoc % :is-legal-move true))))

(rf/reg-event-db
  :begin-move
  (undoable "Begin Move")
  (fn [{:keys [current-scene-idx] :as db} [_ character]]
    (if (:already-moved? character)
      (assoc db :message "Character has already moved!")
      (-> db
        (update-in [:scenes current-scene-idx :gridmap]
                   (partial begin-move character))
        (assoc :moving-character character)))))

(defn clear-legal-moves
  [gridmap]
  (update-tiles gridmap any? #(dissoc % :is-legal-move)))

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
        steps (subvec path 1 (count path))
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
               (dissoc :moving-character)))
     :fx [[:dispatch [:update-intentions]]]}))
