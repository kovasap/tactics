(ns app.interface.view.gridmap
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as st]
            [app.interface.config :refer [debug]]
            [app.interface.utils :refer [get-only]]))


(defn tile-view
  [{:keys [land
           row-idx
           col-idx]
    :as   tile}]
  (let []
    [:div.tile
     {:style         {:font-size  "12px"
                      :text-align "center"
                      :aspect-ratio "1"
                      :position   "relative"}
      :on-mouse-over #()
      :on-mouse-out  #()
      :on-click      #()}
     [:div.background
      {:style (merge (:style land)
                     {:width    "100%"
                      :height   "100%"
                      :position "absolute"
                      :z-index  -1
                      :opacity  0.7})}]
     [:div {:style {:position "absolute" :padding-top "10px" :width "100%"}}
      [:div {:style {:display (if debug "block" "none")}}
       row-idx
       ", "
       col-idx]]]))


(defn gridmap-view
  []
  (let [gridmap @(rf/subscribe [:current-gridmap])]
    (into
      [:div.gridmap
       {:style {:display "grid"
                :grid-template-columns (st/join " " (for [_ gridmap] "1fr"))
                :grid-gap "1px"}}]
      (reduce concat
        (for [column gridmap] (for [tile column] (tile-view tile)))))))
