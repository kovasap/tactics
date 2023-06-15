(ns app.interface.conversations
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  :current-conversation-text
  (fn [db _]
    (:current-conversation-text db)))
