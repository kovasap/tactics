(ns app.interface.view.attacks
  (:require [app.interface.attacking
             :refer
             [get-weapon-damage get-damage-reduction calc-damage]]
            [app.interface.constant-game-data :refer [weapons]]))


(defn detail-attack-view
  [{:keys [attacker defender advantage] :as attack}]
  [:div
   [:div
    "Attacker: "
    (:full-name attacker)
    [:div "Weapon damage " (get-weapon-damage attacker)]
    (if (= advantage :attacker) [:div "Attacking with advantage!"] nil)]
   [:div
    "Defender "
    (:full-name defender)
    [:div "Damage reduction " (get-damage-reduction defender)]]
   [:div "Final damage: " (calc-damage attack)]])

(defn defender-hover-attack-view
  [{:keys [advantage] {:keys [equipped-weapon]} :defender :as attack}]
  [:div {:style {:color (case advantage
                          :attacker "red"
                          :defender "green"
                          :else     "black")}}
   ; TODO make this look nicer
   [:img {:src (:image (equipped-weapon weapons))}]
   (str (case advantage
          :attacker "↓"
          :defender "↑"
          :else     "")
        " "
        (calc-damage attack)
        " dmg")])
  
  
