(ns app.interface.animations
  (:require [re-frame.core :as rf]))

(def time-between-frames-ms 100)

; TODO add movement of the actual character to this via some kind of offset
; parameter, so that the character moves in the direction of e.g. their
; attack.
(rf/reg-event-fx
  :play-animation
  (fn [_
       [_ {:keys [animations image] :as character}
        animation]]
    {:fx (into
           []
           (for [[i frame] (map-indexed vector
                                        (conj (animation animations) image))]
             [:dispatch-later {:ms       (* i time-between-frames-ms)
                               :dispatch [:update-image character frame]}]))}))

(defn get-animation-duration
  [character animation]
  (* time-between-frames-ms (count (animation (:animations character)))))

(rf/reg-event-db
  :update-image
  (fn [db [_ {:keys [full-name]} image-path]]
    (update-in db [:characters full-name] #(assoc % :image image-path))))
