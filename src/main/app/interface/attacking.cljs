(ns app.interface.attacking
 (:require
  [re-frame.core :as rf]
  [day8.re-frame.undo :as undo :refer [undoable]]
  [app.interface.character :refer [weapons]]
  [app.interface.gridmap :refer [update-tiles get-characters-current-tile]]))

(defn distance
  [{from-row-idx :row-idx from-col-idx :col-idx}
   {to-row-idx :row-idx to-col-idx :col-idx}]
  (+ (abs (- from-row-idx to-row-idx))
     (abs (- from-col-idx to-col-idx))))

(defn get-attack-range
  [{:keys [equipped-weapon]}]
  (:range (equipped-weapon weapons)))
  
(defn begin-attack
  [character gridmap]
  (let [from-tile (get-characters-current-tile gridmap character)]
   (update-tiles
    gridmap
    (fn [tile]
     (> (inc (get-attack-range character))
        (distance from-tile tile)
        0))
    #(assoc % :is-legal-attack true))))

(rf/reg-event-db
  :begin-attack
  (undoable "Begin Attack")
  (fn [{:keys [current-scene-idx] :as db} [_ character]]
    (-> db
      (update-in [:scenes current-scene-idx :gridmap]
                 (partial begin-attack character))
      (assoc :attacking-character character))))

(defn clear-legal-attacks
  [gridmap]
  (update-tiles gridmap #(dissoc % :is-legal-attack)))

(rf/reg-event-db
  :cancel-attack
  (fn [{:keys [current-scene-idx] :as db} _]
    (-> db
      (update-in [:scenes current-scene-idx :gridmap] clear-legal-attacks)
      (dissoc :attacking-character))))

(rf/reg-event-db
 :declare-attack-intention
 (undoable "Begin Attack")
 (fn [{:keys [current-scene-idx] :as db} [_ target-full-name]]
  (prn
   (str (:attacking-character db) " is intending to attack " target-full-name))
  (-> db
      (assoc-in [:characters target-full-name :under-attack-by]
                (:attacking-character db))
      (update-in [:scenes current-scene-idx :gridmap] clear-legal-attacks)
      (dissoc :attacking-character))))

(rf/reg-sub
  :attacking-character
  (fn [db _]
    (:attacking-character db)))
