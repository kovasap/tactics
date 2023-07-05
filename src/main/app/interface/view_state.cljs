(ns app.interface.view-state
  (:require 
    [re-frame.core :as rf]))

(rf/reg-event-db
  :toggle-character-leveling-pane
  (fn [db [_ character-full-name]]
    (if (= character-full-name (:currently-leveling-full-name db))
      (dissoc db :currently-leveling-full-name)
      (assoc db :currently-leveling-full-name character-full-name))))

(rf/reg-sub
 :currently-leveling-full-name
 (fn [db _]
   (:currently-leveling-full-name db)))
