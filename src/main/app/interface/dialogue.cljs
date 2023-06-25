(ns app.interface.dialogue 
  (:require
    [re-frame.core :as rf]
    [app.interface.re-frame-utils :refer [dispatch-sequentially-with-timings]]))

(rf/reg-sub
  :current-dialogue-line
  (fn [db _]
    (first (:dialogue-queue db))))
  
(rf/reg-event-db
  :advance-dialogue
  (fn [db _]
    (update db :dialogue-queue pop)))
  
(rf/reg-event-db
  :update-dialogue
  (fn [db [_ dialogue]]
    (update db :dialogue-queue #(apply list (concat % dialogue)))))
