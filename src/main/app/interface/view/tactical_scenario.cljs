(ns app.interface.view.tactical-scenario
  (:require [app.interface.view.gridmap :refer [gridmap-view tile-info-view]]
            [app.interface.view.character :refer [character-info-view]]
            [re-frame.core :as rf]))

(defn hover-panel
  []
  (let [hovered-element      @(rf/subscribe [:hovered-element])
        hovered-element-type @(rf/subscribe [:hovered-element-type])]
    (cond (= hovered-element-type :character) [character-info-view
                                               hovered-element]
          (= hovered-element-type :tile) [tile-info-view hovered-element])))

(defn battle-preview-panel
  []
  (let [{:keys [under-attack-by full-name]} @(rf/subscribe [:hovered-element])
        hovered-element-type      @(rf/subscribe [:hovered-element-type])]
    (if (and (= hovered-element-type :character)
             (not (empty? under-attack-by)))
      [:div {:style {:display "grid"
                     :grid-template-columns "auto auto"
                     :grid-gap "5px"}}
       ; TODO show battle calcultation here
       [:div "Attacker: " (str (mapv :full-name under-attack-by))]
       [:div "Defender " full-name]]
      nil)))

(defn tactical-scenario
  [{:keys [gridmap]}]
  [:div {:style {:display "grid"
                 :grid-template-columns "auto auto"
                 :grid-gap "10px"}}
   [gridmap-view gridmap]
   [:div [hover-panel]
         [battle-preview-panel]]])
