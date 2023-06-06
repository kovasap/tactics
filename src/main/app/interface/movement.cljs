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
                    (> (inc (get-in character [:stats :move]))
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
   

(defn move
  [{:keys [full-name] :as character}
   {to-row-idx :row-idx to-col-idx :col-idx}
   gridmap]
  (let [{from-row-idx :row-idx from-col-idx :col-idx :as tile}
        (get-current-tile gridmap character)]
    (-> gridmap
        (clear-legal-moves)
        (assoc-in [from-row-idx from-col-idx :intention-character-full-name]
                  nil)
        (assoc-in [to-row-idx to-col-idx :intention-character-full-name]
                  full-name))))

(rf/reg-event-fx
  :move
  (undoable "Move")
  (fn [cofx [_ tile]]
    {:db (let [{:keys [current-scene-idx moving-character] :as db} (:db cofx)]
           (-> db
               (update-in [:scenes current-scene-idx :gridmap]
                          (partial move moving-character tile))
               (assoc-in [:characters
                          (:full-name moving-character)
                          :already-moved?]
                         true)
               (dissoc :moving-character)))
     :fx [[:dispatch [:update-intentions]]]}))
