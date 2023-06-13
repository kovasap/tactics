(ns app.interface.attacking
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.undo :as undo :refer [undoable]]
            [app.interface.constant-game-data :refer [weapons]]
            [app.interface.character-stats :refer [get-max-health]]
            [app.interface.animations :refer [get-animation-duration]]
            [app.interface.gridmap
             :refer
             [update-tiles
              get-characters-current-tile
              get-characters-current-intention-tile]]))

(defn distance
  [{from-row-idx :row-idx from-col-idx :col-idx}
   {to-row-idx :row-idx to-col-idx :col-idx}]
  (+ (abs (- from-row-idx to-row-idx))
     (abs (- from-col-idx to-col-idx))))

(defn get-attack-range
  [{:keys [equipped-weapon]}]
  (:range (equipped-weapon weapons)))

(defn tile-in-attack-range?
  [character character-tile tile]
  (> (inc (get-attack-range character))
     (distance character-tile tile)
     0))
  
(defn begin-attack
  [character gridmap]
  (update-tiles gridmap
                (partial tile-in-attack-range?
                         character
                         (get-characters-current-intention-tile gridmap
                                                                character))
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

(defn get-attacks
  [attacker defender]
  [; Attack
   {:attacker attacker :defender defender}
   ;Counterattack
   {:attacker defender :defender attacker}])
   ; TODO add more attacks based on the character's speed
   
(rf/reg-event-fx
  :declare-attack-intention
  (undoable "Declare attack intention")
  (fn [{:keys [db]
        {:keys [current-scene-idx attacking-character characters]} :db}
       [_ target-full-name]]
    {:db (do (prn (str attacking-character
                       " is intending to attack "
                       target-full-name))
             (-> db
                 (update
                   :pending-attacks
                   #(into []
                          (concat (get-attacks attacking-character
                                               (characters target-full-name))
                                  %)))
                 (update-in [:scenes current-scene-idx :gridmap]
                            clear-legal-attacks)
                 (dissoc :attacking-character)))
     :fx [[:dispatch [:update-opponent-intentions]]]}))


(defn get-weapon-damage
  [{:keys [equipped-weapon]}]
  (:damage (equipped-weapon weapons)))
 

(defn get-damage-reduction
  [{{:keys [water]} :affinities}]
  water)

(defn calc-damage
  [attacker defender]
  (min (- (get-weapon-damage attacker) (get-damage-reduction defender)) 0))

(defn get-post-attacks-character
  "Get a character after they were involved in the given attacks."
  [{:keys [full-name] :as character} attacks]
  ((apply comp
    (for [{{defender-full-name :full-name} :defender :keys [attacker defender]}
          attacks
          :when (= full-name defender-full-name)]
     (fn [character]
      (update character
              :health
              (fnil #(- % (calc-damage attacker defender))
                    (get-max-health character))))))
   character))

(rf/reg-event-db
  :execute-attack-stat-change
  (fn [db [_ {:keys [defender] :as attack}]]
    (update-in db [:characters (:full-name defender)]
                  #(get-post-attacks-character % [attack]))))

(rf/reg-event-fx
  :execute-attack
  (fn [_
       [_ {:keys [attacker] :as   attack}
        delay-ms]]
    {:fx [[:dispatch-later {:ms       delay-ms
                              :dispatch [:play-animation attacker :attack]}]
          [:dispatch-later
           {:ms       (+ delay-ms (get-animation-duration attacker :attack))
            :dispatch [:execute-attack-stat-change attack]}]]}))

(defn get-duration-of-attacks-ms
  [attacks]
  (reduce +
    (for [{:keys [attacker]} attacks]
      (get-animation-duration attacker :attack))))

(rf/reg-event-db
  :clear-pending-attacks
  (fn [db _]
    (dissoc db :pending-attacks)))

(rf/reg-event-fx
  :execute-attacks
  (fn [{:keys [db]} _]
    {:fx (let [pending-attacks (vector (:pending-attacks db))]
           (vec (concat (for [[i attack] (map-indexed vector
                                                      (:pending-attacks db))
                              :let       [current-delay
                                          (get-duration-of-attacks-ms
                                            (subvec pending-attacks 0 i))]]
                          [:dispatch-later
                           {:ms       current-delay
                            :dispatch [:execute-attack attack current-delay]}])
                        [[:dispatch [:clear-pending-attacks]]])))}))

(rf/reg-sub
  :under-attack?
  (fn [{:keys [pending-attacks]} [_ {:keys [full-name]}]]
    (some #(= full-name %)
          (map #(:full-name (:defender %)) pending-attacks))))

(rf/reg-sub
  :attacking-character
  (fn [db _]
    (:attacking-character db)))

(rf/reg-sub
  :pending-attacks
  (fn [db _]
    (:pending-attacks db)))
