(ns app.interface.movement
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.undo :as undo :refer [undoable]]
    [app.interface.gridmap :refer [update-tiles get-characters-current-tile]]
    [app.interface.pathfinding :refer [get-number-of-path-steps get-path]]
    [app.interface.character-stats :refer [get-steps-left-to-move]]))

(defn begin-move
 [character gridmap]
 (let [from-tile (get-characters-current-tile gridmap character)]
  (update-tiles
   gridmap
   (fn [tile]
    (> (inc (get-steps-left-to-move character))
       ; alternative: as the bird flies distance
       ; (distance from-tile tile)
       (get-number-of-path-steps (get-path gridmap from-tile tile) character)))
   #(assoc % :is-legal-move true))))

(defn reset-movement-status
  [characters]
  (into {}
        (for [[full-name character] characters]
          [full-name (assoc character :tiles-already-moved 0
                                      :has-intention? false)])))

(rf/reg-event-db
  :begin-move
  (undoable "Begin Move")
  (fn [{:keys [current-scene] :as db} [_ full-name]]
    (let [{:keys [characters] :as updated-character-db}
          (update db :characters reset-movement-status)]
      (-> updated-character-db
          (update-in [:scenes current-scene :gridmap]
                     (partial begin-move (characters full-name)))
          (assoc :moving-character (characters full-name))))))

(defn clear-legal-moves
  [gridmap]
  (update-tiles gridmap #(dissoc % :is-legal-move)))

(defn clear-waypoints
  [gridmap]
  (update-tiles gridmap
                (fn [{:keys [waypoint-for]}] waypoint-for)
                (fn [tile] (dissoc tile :waypoint-for))))

(defn clear-intentions
  [gridmap full-name]
  (update-tiles gridmap
                (fn [{:keys [intention-character-full-name]}]
                  (= intention-character-full-name full-name))
                (fn [tile] (dissoc tile :intention-character-full-name))))
  
; TODO fix this
(rf/reg-event-db
  :cancel-move
  (fn [{:keys [current-scene] :as db} _]
    (-> db
      (update-in [:scenes current-scene :gridmap] clear-legal-moves)
      (dissoc :moving-character))))
   

(defn make-move-intention
  [{:keys [full-name]} path gridmap]
  (if (> 2 (count path))
    gridmap
    (let [steps (subvec path 1 (count path))
          {to-row-idx :row-idx to-col-idx :col-idx} (last path)]
      (->
        gridmap
        ; Add waypoints
        ((apply comp
          (for [{path-row-idx :row-idx path-col-idx :col-idx} steps]
           #(assoc-in % [path-row-idx path-col-idx :waypoint-for] full-name))))
        (#(clear-intentions % full-name))
        (assoc-in [to-row-idx to-col-idx :intention-character-full-name]
                  full-name)))))

(defn declare-move-intention
  [character path gridmap]
  (clear-legal-moves (make-move-intention character path gridmap)))

(rf/reg-event-fx
  :preview-move-intention
  (fn [{{:keys [current-scene moving-character] :as db} :db} [_ end-tile]]
    {:db (let [gridmap    (get-in db [:scenes current-scene :gridmap])
               start-tile (get-characters-current-tile gridmap
                                                       moving-character)
               path       (get-path gridmap start-tile end-tile)]
           (-> db
               (update-in [:scenes current-scene :gridmap]
                          (comp (partial make-move-intention
                                         moving-character
                                         path) 
                                clear-waypoints))))
     :fx [[:dispatch [:update-opponent-intentions]]]}))

(rf/reg-event-fx
  :declare-move-intention
  (undoable "Declare move intention")
  (fn [cofx [_ end-tile]]
    {:db (let [{:keys [current-scene moving-character] :as db} (:db cofx)
               gridmap    (get-in db [:scenes current-scene :gridmap])
               start-tile (get-characters-current-tile gridmap
                                                       moving-character)
               path       (get-path gridmap start-tile end-tile)]
           (->
             db
             (update-in [:scenes current-scene :gridmap]
                        (partial declare-move-intention moving-character path))
             (assoc-in [:characters
                        (:full-name moving-character)
                        :tiles-already-moved]
                       (dec (count path)))
             (assoc-in [:characters
                        (:full-name moving-character)
                        :has-intention?]
                       true)
             (dissoc :moving-character)))
     :fx [[:dispatch [:update-opponent-intentions]]]}))

(defn character-moved-from-tile?
  [{:keys [intention-character-full-name character-full-name]}]
  (and character-full-name
       (not (= intention-character-full-name character-full-name))))

(defn character-moved-to-tile?
  [{:keys [intention-character-full-name character-full-name]}]
  (and intention-character-full-name
       (not (= intention-character-full-name character-full-name))))

(defn execute-movements
  [gridmap]
  (-> gridmap
      ; remove old positions for moved characters
      (update-tiles character-moved-from-tile?
                    #(dissoc % :character-full-name))
      ; TODO if hitting a waypoints triggers any effect, do it here
      (clear-waypoints)
      ; add new positions
      (update-tiles
        character-moved-to-tile?
        #(assoc % :character-full-name (:intention-character-full-name %)))))


(rf/reg-event-db
  :execute-intended-movements
  (fn [{:keys [current-scene] :as db}]
    (-> db
      (update-in [:scenes current-scene :gridmap] execute-movements))))

(rf/reg-sub
  :moving-character
  (fn [db _]
    (:moving-character db)))
