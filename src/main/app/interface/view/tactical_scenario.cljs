(ns app.interface.view.tactical-scenario
  (:require [app.interface.view.gridmap :refer [gridmap-view tile-info-view]]
            [app.interface.view.character :refer [character-info-view]]
            [app.interface.attacking
             :refer
             [get-weapon-damage get-damage-reduction calc-damage]]
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
        pending-attacks      @(rf/subscribe [:pending-attacks])]
    (if (and (= hovered-element-type :character)
             (not (empty? pending-attacks)))
      [:div {:style {:display  "grid"
                     :grid-template-columns "auto auto"
                     :grid-gap "5px"}}
       (into [:div]
             (for [{:keys [attacker defender]} pending-attacks
                   :when (= full-name (:full-name defender))]
               [:div
                [:div
                 "Attacker: "
                 (:full-name attacker)
                 [:div "Weapon damage " (get-weapon-damage attacker)]]
                [:div
                 "Defender "
                 full-name
                 [:div "Damage reduction " (get-damage-reduction defender)]]
                [:div "Final damage: " (calc-damage attacker defender)]]))]
      nil)))

(defn tactical-scenario
  [{:keys [gridmap]}]
  [:div {:style {:display "grid"
                 :grid-template-columns "auto auto"
                 :grid-gap "10px"}}
   [gridmap-view gridmap]
   [:div [hover-panel]
         [battle-preview-panel]]])
