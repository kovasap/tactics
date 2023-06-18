(ns app.interface.view.tactical-scenario
  (:require [app.interface.view.gridmap :refer [gridmap-view tile-info-view]]
            [app.interface.view.character :refer [character-info-view]]
            [app.interface.view.attacks :refer [detail-attack-view]]
            [re-frame.core :as rf]))

(defn hover-panel
  []
  (let [hovered-element      @(rf/subscribe [:hovered-element])
        hovered-element-type @(rf/subscribe [:hovered-element-type])]
    (cond (= hovered-element-type :character) [character-info-view
                                                                   hovered-element]
          (= hovered-element-type :tile) [tile-info-view hovered-element])))

; Inspiration:
; https://lparchive.org/Fire-Emblem-Blazing-Sword/Update%2001/18-L01P17.png
(defn battle-preview-panel
  []
  (let [{:keys [full-name]}  @(rf/subscribe [:hovered-element])
        hovered-element-type @(rf/subscribe [:hovered-element-type])
        intended-attacks      @(rf/subscribe [:intended-attacks])]
    (if (and (= hovered-element-type :character)
             (not (empty? intended-attacks)))
      [:div {:style {:display  "grid"
                     :grid-template-columns "auto auto"
                     :grid-gap "5px"}}
       (into [:div]
             (for [{:keys [defender] :as attack} intended-attacks
                   :when (= full-name (:full-name defender))]
               [detail-attack-view attack]))]
      nil)))

(defn tactical-scenario
  [{:keys [gridmap]}]
  [:div {:style {:display "grid"
                 :grid-template-columns "auto auto"
                 :grid-gap "10px"}}
   [gridmap-view gridmap]
   [:div [hover-panel]
         [battle-preview-panel]]])
