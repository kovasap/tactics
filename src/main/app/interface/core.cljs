(ns app.interface.core
  (:require ["react-dom/client" :refer [createRoot]]
            [day8.re-frame.http-fx]
            [day8.re-frame.undo :as undo :refer [undoable]]  
            [goog.dom :as gdom]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [app.interface.sente :refer [send-state-to-server!]]
            [app.interface.view.main :refer [main]]
            [app.interface.utils :refer [get-only]]
            [app.interface.gridmap :refer [parse-gridmap-str]]
            [app.interface.character :refer [starting-characters]]
            ; Included just so reframe events are picked up
            [app.interface.movement]
            [app.interface.attacking]
            [app.interface.intentions]
            [cljs.pprint]
            [taoensso.timbre :as log]))

;; ----------------------------------------------------------------------------
;; Setup

(def scene-one
  {:gridmap (parse-gridmap-str 
              "FM  M   M   F   F   P   P   W  
               W   W   M   F   F   P   P   W
               W   M   F   F   F   P   P   W
               W   M   M   F   F   P   P   W
               W   M   M   F   F   F   P   W
               W   M   M   F   F1  F   F   W
               W   M   M   F   W   F   F   W
               W   M   M   W   W   W   F   W
               S   M   S   S   W   F   F   W
               S   S   S   S   S   F   F   W
               S   S   S   S   S   F   F   W")})

(rf/reg-event-db
  :app/setup
  (fn [db _]
    (-> db
        (assoc :scenes [; {:title :start}
                        scene-one])
        (assoc :characters starting-characters) 
        (assoc :current-scene-idx 0))))

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
