(ns app.interface.view.main
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            ; [ring.middleware.anti-forgery]
            [app.interface.sente :refer [chsk-state login]]
            [app.interface.view.scenes.start :refer [start-scene]]
            [app.interface.view.tactical-scenario :refer [tactical-scenario]]
            [cljs.pprint]))

(rf/reg-sub
  :hovered-element
  (fn [db _]
    (:hovered-element db)))

(rf/reg-sub
  :hovered-element-type
  (fn [db _]
    (:hovered-element-type db)))

(rf/reg-event-db
  :hover-element
  (fn [db [_ element-type element]]
    (assoc db :hovered-element element
              :hovered-element-type element-type)))

(defn undo-button
  []
  ; only enable the button when there's undos
  (let [undos? (rf/subscribe [:undos?])]
    (fn []
      [:button.btn.btn-outline-primary
       {:disabled (not @undos?)
        :on-click #(rf/dispatch [:undo])
        :style {:margin-right "auto"}}
       "Undo"])))


; Not currently necessary/used
(defn login-field
  []
  [:span
   [:input#input-login {:type :text :placeholder "User-id"}]
   [:button.btn.btn-outline-primary
    {:on-click (fn []
                 (let [user-id (.-value (.getElementById js/document
                                                         "input-login"))]
                   (login user-id)))}
    "Secure login!"]])


(defn main
  "Main view for the application."
  []
  [:div @chsk-state]
  [:div.container
   #_(let [csrf-token (force
                        ring.middleware.anti-forgery/*anti-forgery-token*)]
       [:div#sente-csrf-token {:data-csrf-token csrf-token}])
   [:h1 "My App"]
   ; [login-field]
   [:div {:style {:display "flex"}}
    [:button.btn.btn-outline-primary {:on-click #(rf/dispatch [:app/setup])}
     "Reset App"]
    [:button.btn.btn-outline-primary {:on-click #(rf/dispatch [:advance-scene])}
     "Next Scene"]
    [:button.btn.btn-outline-primary {:on-click #(rf/dispatch [:pass-turn])}
     "End turn (enter)"]
    [undo-button]]
   (let [current-scene @(rf/subscribe [:current-scene])]
     (cond
       (= :start (:title current-scene)) [start-scene current-scene]
       (contains? current-scene :gridmap) [tactical-scenario current-scene]
       :else [:div "default"]))
   [:div @(rf/subscribe [:message])]])
