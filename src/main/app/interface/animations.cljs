(ns app.interface.animations
  (:require
    [re-frame.core :as rf]
    [app.interface.re-frame-utils :refer [dispatch-sequentially-with-timings]]
    [app.interface.constant-game-data :refer [character-classes]]))

(def time-between-frames-ms 80)

; TODO add movement of the actual character to this via some kind of offset
; parameter, so that the character moves in the direction of e.g. their
; attack.
(rf/reg-event-fx
  :play-animation
  (fn [_
       [_ {:keys [class-keyword] :as character}
        animation]]
    (let [image-paths (conj
                        (for [i (range (animation (:animation-frames
                                                    (class-keyword
                                                      character-classes))))]
                          (str "class-images/"
                               (name class-keyword)
                               "/"
                               (name animation)
                               "/"
                               (inc i)
                               ".png"))
                        (str "class-images/"
                             (name class-keyword)
                             "/idle.png"))]
      {:fx (dispatch-sequentially-with-timings
             (for [image image-paths]
              [[:update-image character image]
               time-between-frames-ms]))})))

(defn get-animation-duration
  [{:keys [class-keyword] :as character} animation]
  (* time-between-frames-ms
     (animation (:animation-frames (class-keyword character-classes)))))

(rf/reg-event-db
  :update-image
  (fn [db [_ {:keys [full-name]} image-path]]
    (update-in db [:characters full-name] #(assoc % :image image-path))))


(def experience-gain-duration-ms)

(rf/reg-event-fx
  :animate-experience-gains
  (fn [{:keys [db]} [_ pre-attacks-db]]
    {:db db
     :fx []}))
    ; TODO
    ; Revert all experience gains to pre-attack-db
    ; Then, toggle visibility of experience bars on all characters
    ; Then, find the delta for all experence types for all chararcters between
    ; the current db and the pre-attacks-db
    ; Then slowly change the values in the db from pre-attacks to post
