(ns app.interface.attacking
 (:require
  [re-frame.core :as rf]
  [day8.re-frame.undo :as undo :refer [undoable]]
  [app.interface.constant-game-data :refer [weapons]]
  [app.interface.gridmap :refer [update-tiles get-characters-current-tile]]))

(defn distance
  [{from-row-idx :row-idx from-col-idx :col-idx}
   {to-row-idx :row-idx to-col-idx :col-idx}]
  (+ (abs (- from-row-idx to-row-idx))
     (abs (- from-col-idx to-col-idx))))

(defn get-attack-range
  [{:keys [equipped-weapon]}]
  (:range (equipped-weapon weapons)))

(defn tile-in-attack-range?
  [character gridmap tile]
  (> (inc (get-attack-range character))
     (distance (get-characters-current-tile gridmap character) tile)
     0))
  
(defn begin-attack
  [character gridmap]
  (update-tiles gridmap
                (partial tile-in-attack-range? character gridmap)
                #(assoc % :is-legal-attack true)))

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
  (fn [{:keys [current-scene-idx]
        :as   db}
       [_ target-full-name]]
    (prn (str (:attacking-character db)
              " is intending to attack "
              target-full-name))
    (-> db
        (update-in [:characters target-full-name :under-attack-by]
                   #(if (nil? %)
                      []
                      (conj % (:attacking-character db))))
        (update-in [:scenes current-scene-idx :gridmap] clear-legal-attacks)
        (dissoc :attacking-character))))

(defn get-max-health
  [character]
  (:earth (:affinities character)))

(defn get-health
  [{:keys [health]
    :as   character}]
  (if health health (get-max-health character)))
    

(defn calc-damage
  [{:keys [equipped-weapon]
    :as   attacker}
   defender]
  (- (:damage (equipped-weapon weapons)) (:water (:affinities defender))))
  
(defn commit-attacks
  [characters]
  (into {}
        (for [{:keys [full-name under-attack-by]
               :as   character}
              (vals characters)]
          [full-name
           ((apply comp
             (for [attacker under-attack-by]
              (fn [defender]
               (update defender
                       :health
                       #(- (get-health defender)
                           (calc-damage attacker defender))))))
            character)])))

(rf/reg-sub
  :attacking-character
  (fn [db _]
    (:attacking-character db)))
