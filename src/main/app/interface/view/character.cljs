(ns app.interface.view.character
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [app.interface.character-stats
     :refer
     [get-health
      get-max-health
      calc-speed
      calc-move-range
      calc-max-health
      calc-power
      calc-defense
      calc-sneak
      calc-ambition
      calc-insight
      experience-to-next-level]]
    ; [app.interface.view.attacks :refer [defender-hover-attack-view]]
    [app.interface.constant-game-data :refer [character-classes weapons]]))

(defn character-name
  [{:keys [controlled-by-player? full-name]}]
  [:span {:style {:color (if controlled-by-player? "blue" "black")}}
        full-name])

(defn- common-character-view
  [{:keys [experience
           level
           image
           controlled-by-player?
           has-intention?
           dead
           full-name
           equipped-weapon]
    :as   character}
   is-intention?]
  (let [hovered-element     @(rf/subscribe [:hovered-element])
        is-hovered?         (= character hovered-element)
        next-turn-character @(rf/subscribe [:next-turn-character character])
        moving-character    @(rf/subscribe [:moving-character])
        attacks             @(rf/subscribe [:attacks-targeting-character
                                            character])]
    (if character
      [:div {:on-mouse-over #(rf/dispatch [:hover-element
                                           :character
                                           character])
             :on-mouse-out  #()
             :style         {:z-index 2}}
       [:div.character
        {:style {:position   "absolute"
                 :overflow   "visible"
                 :text-align "left"
                 :top        50
                 :z-index    50
                 :display    (if (and controlled-by-player?
                                      is-hovered?
                                      (not moving-character)
                                      (not dead))
                               "block"
                               "none")}}
        [:button.btn.btn-outline-primary
         {:on-click #(if @(rf/subscribe [:moving-character])
                       (rf/dispatch [:cancel-move])
                       (rf/dispatch [:begin-move full-name]))}
         "Move"]
        ; Moving into another characters range automatically fights if the
        ; other character sticks around
        #_[:button.btn.btn-outline-primary
           {:on-click #(if @(rf/subscribe [:attacking-character])
                         (rf/dispatch [:cancel-attack])
                         (rf/dispatch [:begin-attack character]))}
           "Attack"]
        [:button.btn.btn-outline-primary {:on-click #()}
         "Re-equip"]
        [:button.btn.btn-outline-primary {:on-click #()}
         "Utility"]]
       [character-name character]
       [:br]
       [:img {:style {:opacity   (if is-intention? 0.2 1.0)
                      :transform (if dead "rotate(90deg)" nil)
                      :z-index   20
                      :filter    (if (not (empty? attacks))
                                   "drop-shadow(0px 0px 20px red)"
                                   nil)}
              :src   image}]
       #_(if (= has-intention? is-intention?)
           (into [:div {:style {:position    "absolute"
                                :z-index     8
                                :left        "35%"
                                :top         "-10%"
                                :white-space "nowrap"}}]
                 (for [attack attacks]
                   [defender-hover-attack-view attack]))
           nil)
       ; Experience
       [:div {:style {:position    "absolute"
                      :z-index     10
                      :left        "0%"
                      :top         "50%"
                      :white-space "nowrap"}}
        [:div "Exp"]
        [:div (str experience " / " experience-to-next-level)]
        [:div "↓"]
        [:div
         (str (:experience next-turn-character)
              " / "
              experience-to-next-level)]]
       ; Health
       [:div {:style {:position    "absolute"
                      :z-index     10
                      :left        "70%"
                      :top         "50%"
                      :white-space "nowrap"}}
        [:div "HP"]
        [:div (str (get-health character) " / " (get-max-health character))]
        [:div "↓"]
        [:div {:style {:color (if (:dead next-turn-character) "red" "black")}}
         (str (get-health next-turn-character)
              " / "
              (get-max-health next-turn-character))]]
       [:div {:style {:position    "absolute"
                      :z-index     10
                      :left        "15%"
                      :top         "40%"
                      :white-space "nowrap"}}
        [:img {:src (:image (equipped-weapon weapons))}]]]
      nil)))

(defn character-view
  [character]
  (common-character-view character false))

(defn intention-character-view
  [character]
  (common-character-view character true))


(defn element-view
  [{:keys [light dark air water earth fire] :as affinities}]
  ; https://stackoverflow.com/a/43958912
  [:div {:style {:display       "grid"
                 :grid-gap      "10px"
                 :justify-items "center"
                 :text-align    "center"
                 :grid-template-columns "repeat(2, 200px)"}}
   [:div {:style {:grid-row "1" :grid-column "span 2"}}
    [:b "Light " light] " / " (calc-insight affinities) " Insight"
    [:p.affinity-desc "All-seeing, idealistic, non-interventive."]
    [:p.affinity-desc
     "Insight determines whether other characters will be able to see this
     characters intention or not before they make their move. If the other
     character has a lower insight, they will move to where this character is
     before their move if they are trying to attack this character."]]
   [:div
    [:b "Air " air] " / " (calc-speed affinities) " Speed / "
    (calc-move-range affinities) " Base Move Range"
    [:p.affinity-desc "Impulsive, fast, carefree."]
    [:p.affinity-desc
     "Speed determines who will attack first in a fight without advantage.
     It also determines the order in which attacks will be made; faster
     characters will be attacked last - or not attacked at all if the attacker
     does have sufficient ambition!"]
    [:p.affinity-desc
     "Base Move Range is the distance in steps a character can move in a turn.
     Note that different tiles take a different number of steps to move do
     based on their affinities and the moving character's affinities
     (TODO implement this by changing pathfinding.get-steps-to-move-to)!"]]
   [:div
    [:b "Water " water] " / " (calc-defense affinities) " Defense"
    [:p.affinity-desc "Meditative, redirecting."]
    [:p.affinity-desc
     "Defense is directly subtracted from all damage received."]]
   [:div
    [:b "Fire " fire] " / " (calc-power affinities) " Power"
    [:p.affinity-desc "Impulsive, powerful."]
    [:p.affinity-desc "Power is directly added to all damage dealt."]]
   [:div
    [:b "Earth " earth] " / " (calc-max-health affinities) " Max Health"
    [:p.affinity-desc "Meditative, disrupting."]
    [:p.affinity-desc
     "Max health is the total amount of health this character can have."]]
   [:div {:style {:grid-row "4" :grid-column "span 2"}}
    [:b "Dark " dark] " / " (calc-sneak affinities) " Sneak / "
    (calc-ambition affinities) " Ambition"
    [:p.affinity-desc "Short-sighted, pragmatic, action-oriented."]
    [:p.affinity-desc
     "Characters with higher
      sneak will not be targeted for attacks if there is a character with
      lower sneak to target instead (TODO implement or remove)."]
    [:p.affinity-desc
     "Ambition is the number of attacks a character can make in a turn
     (including counterattacks)."]]])

; TODO Turn health bar/ratio red if the character can be one shot by an enemy
; on the map.

(defn character-leveling-panel
  [character]
  [:div {:style         {:display          "grid"
                         :grid-gap         "0px"
                         :grid-template-columns "200px"}}
   (into [:div]
         (for [[kw {:keys [prequisite-affinities]}] character-classes]
           [:div (str (name kw) " " prequisite-affinities)]))])

(defn character-info-view
  [{:keys [class-keyword
           affinities
           weapon-levels
           image
           selected-for-chapter?
           full-name]
    :as   character}]
  (let [leveling-view? (= full-name
                          @(rf/subscribe [:currently-leveling-full-name]))]
    [:div {:style         {:display          "grid"
                           :grid-gap         "0px"
                           :justify-items    "center"
                           :text-align       "center"
                           :transition "all .5s linear"
                           :background-color (if selected-for-chapter?
                                               "green"
                                               "grey")
                           :grid-template-columns (if leveling-view?
                                                    "400px 200px"
                                                    "400px 0px")}
           :on-mouse-over #(rf/dispatch [:play-animation character :attack])
           :on-mouse-out  #()
           :on-click      #(rf/dispatch [:toggle-select-for-chapter
                                         full-name])}
     [:div
      [character-name character]
      [:img {:src image}]
      [:p (name class-keyword)]
      [:p (get-health character) " / " (get-max-health character) " health"]
      [:p (str weapon-levels)]
      [:button.btn.btn-outline-primary
       {:on-click #(rf/dispatch [:toggle-character-leveling-pane full-name])}
       "Level Up / Reclass"]
      [element-view affinities]]
     [:div {:style {;:transition "all .5s linear"
                    :background-color "red"
                    :display    (if leveling-view? "block" "none")
                    :width      (if leveling-view? "100%" "0%")}}
      [character-leveling-panel character]]]))
