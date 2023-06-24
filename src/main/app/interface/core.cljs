(ns app.interface.core
  (:require ["react-dom/client" :refer [createRoot]]
            [day8.re-frame.http-fx]
            [day8.re-frame.undo :as undo :refer [undoable]]  
            [goog.dom :as gdom]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [app.interface.view.main :refer [main]]
            [app.interface.utils :refer [get-only]]
            [app.interface.initial-db :refer [initial-db]]
            ; Included just so reframe events are picked up
            [app.interface.movement]
            [app.interface.attacking]
            [app.interface.intentions]
            [app.interface.keyboard]
            [cljs.pprint]))

(rf/reg-event-fx
  :app/setup
  (fn [_ _]
    {:db initial-db
     :fx [[:dispatch [:update-opponent-intentions]]]}))

(rf/reg-sub
 :characters-by-full-name
 (fn [db _]
   (:characters db)))

(rf/reg-event-fx
  :pass-turn
  (fn [{:keys [db]} _]
    {:fx [[:dispatch [:execute-intentions]]
          [:dispatch [:animate-experience-gains db]]]}))

(rf/reg-event-db
  :advance-scene
  (undoable "Advancing scene")
  (fn [db _]
    (update db :current-scene-idx inc)))

(rf/reg-sub
  :current-scene
  (fn [db _]
    (get (:scenes db) (:current-scene-idx db))))

(rf/reg-event-db
  :message
  (undoable "Send message")
  (fn [db [_ message]]
    (assoc db :message message)))

(rf/reg-sub
  :message
  (fn [db _]
    (:message db)))


;; -- Entry Point -------------------------------------------------------------

(defonce root (createRoot (gdom/getElement "app")))

(defn init
  []
  (rf/dispatch [:app/setup])
  (.render root (r/as-element [main])))

(defn- ^:dev/after-load re-render
  "The `:dev/after-load` metadata causes this function to be called after
  shadow-cljs hot-reloads code. This function is called implicitly by its
  annotation."
  []
  (rf/clear-subscription-cache!)
  (init))
