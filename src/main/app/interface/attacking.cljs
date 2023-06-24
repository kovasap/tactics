(ns app.interface.attacking
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.undo :as undo :refer [undoable]]
    [app.interface.constant-game-data :refer [weapons weapon-advantages]]
    [app.interface.character-stats
     :refer
     [get-max-health experience-to-next-level]]
    [app.interface.animations :refer [get-animation-duration]]
    [app.interface.gridmap
     :refer
     [update-tiles
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

(defn- get-attack-weapon-advantage
  [{attacker-weapon :equipped-weapon} {defender-weapon :equipped-weapon}]
  (cond
    (contains? (weapon-advantages attacker-weapon) defender-weapon) :attacker
    (contains? (weapon-advantages defender-weapon) attacker-weapon) :defender
    :else nil))

(defn make-attack
  [attacker defender]
  {:attacker  attacker
   :defender  defender
   :advantage (get-attack-weapon-advantage attacker defender)})

; TODO group these attacks into rounds so that if one unit is killed by an
; attack they don't get a counterattack
(defn get-attacks
  [attacker defender]
  [; Attack
   (make-attack attacker defender)
   ;Counterattack
   (make-attack defender attacker)])
   ; TODO add more attacks based on the character's speed
;  ; If I do this then I'll have to think about how experience is allocated for
;  ; these additional attacks.
   
(defn clear-attacks-involving-character
  [db character]
  (update db
          :intended-attacks
          #(filter (fn [{:keys [attacker defender]}]
                     (not (or (= (:full-name character) (:full-name attacker))
                              (= (:full-name character)
                                 (:full-name defender)))))
             %)))
  

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
                 (update :intended-attacks
                         #(concat (get-attacks attacking-character
                                               (characters target-full-name))
                                  %))
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
  [{:keys [attacker defender advantage]}]
  (max (- (* (if (= advantage :attacker) 2 1)
             (get-weapon-damage attacker))
          (get-damage-reduction defender))
       0))


(declare get-experience-from-attack)

(defn get-weapon-level-from-attack
  [attack]
  1)

(defn get-post-attacks-character
  "Get a character after they were involved in the given attacks."
  [{:keys [full-name] :as character} attacks]
  (->
    character
    ; Take damage from attack if you are defending
    ((apply comp
      (for [{{defender-full-name :full-name} :defender :as attack} attacks
            :when (= full-name defender-full-name)]
       (fn [character]
        (-> character
            (update :health
                    (fnil #(- % (calc-damage attack))
                          (get-max-health character)))
            (#(if (> (:health %) 0) % (assoc % :dead true))))))))
    ; Get experience from attack if you are attacking
    ((apply comp
      (for [{{attacker-full-name :full-name :keys [equipped-weapon]} :attacker
             :as attack}
            attacks
            :when (= full-name attacker-full-name)]
       (fn [character]
        (let [exp-gained    (get-experience-from-attack attack)
              levels-gained (int (/ exp-gained experience-to-next-level))
              new-exp (mod exp-gained experience-to-next-level)]
         (-> character
             (assoc :experience new-exp)
             (update :level #(+ % levels-gained))
             (update-in [:weapon-levels equipped-weapon]
                        (fnil #(+ % (get-weapon-level-from-attack attack))
                              0))))))))))

(defn get-experience-from-attack
  "Experience the attacker should get from making an attack."
  [{:keys [attacker defender] :as attack}]
  (let [post-attack-defender (get-post-attacks-character defender [attack])
        kill-bonus (if (:dead post-attack-defender) 50 0)]
    (+ 30 (* 10 (:level defender) (:level attacker)) kill-bonus)))

(rf/reg-event-db
  :execute-attack-stat-change
  (fn [db [_ {:keys [attacker defender] :as attack}]]
    (-> db
      (update-in [:characters (:full-name defender)]
                 #(get-post-attacks-character % [attack]))
      (update-in [:characters (:full-name attacker)]
                 #(get-post-attacks-character % [attack])))))

(rf/reg-event-fx
  :execute-attack
  (fn [_
       [_ {:keys [attacker] :as attack}
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
  :clear-intended-attacks
  (fn [db _]
    (dissoc db :intended-attacks)))

(rf/reg-event-fx
  :execute-intended-attacks
  (fn [{:keys [db]} _]
    {:fx (let [intended-attacks (:intended-attacks db)]
           (vec (concat (for [[i attack] (map-indexed vector
                                                      (:intended-attacks db))
                              :let       [current-delay
                                          (get-duration-of-attacks-ms
                                            (subvec intended-attacks 0 i))]]
                          [:dispatch-later
                           {:ms       current-delay
                            :dispatch [:execute-attack attack current-delay]}])
                        [[:dispatch [:clear-intended-attacks]]])))}))

(rf/reg-sub
  :attacks-targeting-character
  (fn [{:keys [intended-attacks]} [_ {:keys [full-name]}]]
    (filter #(= full-name (:full-name (:defender %))) intended-attacks)))

(rf/reg-sub
  :attacking-character
  (fn [db _]
    (:attacking-character db)))

(rf/reg-sub
  :intended-attacks
  (fn [db _]
    (:intended-attacks db)))
