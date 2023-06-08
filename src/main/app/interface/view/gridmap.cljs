(ns app.interface.view.gridmap
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as st]
            [app.interface.view.character
             :refer
             [character-view intention-character-view]]
            [app.interface.config :refer [debug]]
            [app.interface.utils :refer [get-only]]))


(def tile-size "120px")


(defn tile-view
  [{:keys [land
           row-idx
           col-idx
           character-full-name
           is-legal-move
           waypoint-for
           intention-character-full-name]
    :as   tile}]
  (let [character (get @(rf/subscribe [:characters-by-full-name])
                       character-full-name)
        intention-character (get @(rf/subscribe [:characters-by-full-name])
                                 intention-character-full-name)]
    [:div.tile {:style         {:font-size    "12px"
                                :text-align   "center"
                                :height       tile-size
                                :width        tile-size
                                :aspect-ratio "1"
                                :position     "relative"}
                :on-mouse-over #()
                :on-mouse-out  #()
                :on-click      #(cond is-legal-move (rf/dispatch
                                                      [:declare-move-intention
                                                       tile]))}
     [:div.background {:style (merge (:style land)
                                     {:width    "100%"
                                      :height   "100%"
                                      :position "absolute"
                                      :z-index  -1
                                      :opacity  (if is-legal-move 0.9 0.7)})}]
     [:div {:style {:position "absolute" :padding-top "10px" :width "100%"}}
      [:div {:style {:display (if debug "block" "none")}}
       row-idx
       ", "
       col-idx]
      (if waypoint-for [:span "wp"] nil)
      [character-view character]
      [intention-character-view intention-character]]]))


(defn gridmap-view
  []
  (let [gridmap @(rf/subscribe [:current-gridmap])]
    (into [:div.gridmap
           {:style {:display  "grid"
                    :grid-template-columns (st/join " "
                                                    (for [_ (first gridmap)]
                                                      tile-size))
                    :grid-gap "1px"}}]
          (reduce concat
            (for [column gridmap] (for [tile column] (tile-view tile)))))))
