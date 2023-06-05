(ns app.interface.view.tactical-scenario
  (:require [app.interface.view.gridmap :refer [gridmap-view]]))

(defn tactical-scenario
  [{:keys [gridmap]}]
  [:div
   [gridmap-view gridmap]])
