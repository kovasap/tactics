(ns app.interface.actions.action
  (:require
    [re-frame.core :as rf]))

(defn doable?
  [action {:keys [gridmap characters]}])

(defn apply-actions
  "Execute actions, updating and returning the current-scenario."
  [actions current-scenario])

(rf/reg-event-db
 :attemping-queueing-action
 (fn [{:keys [current-scenario] :as db} [_ action]]
   ; Check if the action is doable after all other actions in the queue have
   ; been executed. 
   (if (doable? action (apply-actions 
                         (:action-queue current-scenario)
                         current-scenario))
     (update-in db [:current-scenario :action-queue] conj action)
     (assoc db :message "Could not execute action!"))))

(defn attemping-queueing-action
  [action])
