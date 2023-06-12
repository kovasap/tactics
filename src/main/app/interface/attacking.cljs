(ns app.interface.attacking
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [day8.re-frame.undo :as undo :refer [undoable]]
            [app.interface.constant-game-data :refer [weapons]]
            [app.interface.character-stats :refer [get-health]]
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

(rf/reg-event-fx
  :declare-attack-intention
  (undoable "Declare attack intention")
  (fn [{:keys [db] {:keys [current-scene-idx]} :db} [_ target-full-name]]
    {:db (do (prn (str (:attacking-character db)
                       " is intending to attack "
                       target-full-name))
             (-> db
                 (update-in [:characters target-full-name :under-attack-by]
                            #(into [(:attacking-character db)] %))
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


(def time-between-frames-ms 100)

; TODO add movement of the actual character to this via some kind of offset
; parameter, so that the character moves in the direction of e.g. their attack.
(rf/reg-event-fx
  :play-animation
  (fn [cofx
       [_ {:keys [animations image] :as character}
        animation]]
    {:fx (into
           []
           (for [[i frame] (map-indexed vector
                                        (conj (animation animations) image))]
             [:dispatch-later {:ms       (* i time-between-frames-ms)
                               :dispatch [:update-image
                                          character
                                          frame]}]))}))

(rf/reg-event-db
  :update-image
  (fn [db [_ {:keys [full-name]} image-path]]
    (update-in db [:characters full-name] #(assoc % :image image-path))))

  
; TODO make the attack a battle with both sides potentially doing damage
; multiple times based on speed (like in fire emblem)
; To animate this we might need to make a list of all animation frames from
; both characters at once.
(defn commit-attacks
  [characters]
  (into {}
        (for [{:keys [full-name under-attack-by]
               :as   character}
              (vals characters)]
          (do
            ; TODO move this somewhere else
            (rf/dispatch [:play-animation (first under-attack-by) :attack])
            [full-name
             (-> character
                 ((apply comp
                   (for [attacker under-attack-by]
                    (fn [defender]
                     (assoc defender
                      :health (- (get-health defender)
                                 (calc-damage attacker defender)))))))
                 (dissoc :under-attack-by))]))))

(rf/reg-sub
  :attacking-character
  (fn [db _]
    (:attacking-character db)))
