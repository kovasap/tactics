(ns app.interface.view.tactical-scenario
  (:require [app.interface.view.gridmap :refer [gridmap-view]]
            [app.interface.view.character :refer [character-stats-view]]
            [re-frame.core :as rf]))

(defn hover-panel
  []
  (let [hovered-element      @(rf/subscribe [:hovered-element])
        hovered-element-type @(rf/subscribe [:hovered-element-type])]
    (cond (= hovered-element-type :character) [character-stats-view
                                               hovered-element])))

(defn tactical-scenario
  [{:keys [gridmap]}]
  [:div {:style {:display "grid"
                 :grid-template-columns "auto auto"
                 :grid-gap "10px"}}
   [gridmap-view gridmap]
   [hover-panel]])
