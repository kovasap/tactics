(ns app.interface.animations
  (:require [re-frame.core :as rf]
            [app.interface.constant-game-data :refer [character-classes]]))

(def time-between-frames-ms 100)

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
                               i
                               ".png"))
                        (str "class-images/"
                             (name class-keyword)
                             "/idle.png"))]
      {:fx (into []
                 (for [[i image] (map-indexed vector image-paths)]
                   [:dispatch-later
                    {:ms       (* i time-between-frames-ms)
                     :dispatch [:update-image character image]}]))})))

(defn get-animation-duration
  [{:keys [class-keyword]} animation]
  (* time-between-frames-ms
     (animation (:animation-frames (class-keyword character-classes)))))

(rf/reg-event-db
  :update-image
  (fn [db [_ {:keys [full-name]} image-path]]
    (update-in db [:characters full-name] #(assoc % :image image-path))))
