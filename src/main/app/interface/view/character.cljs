(ns app.interface.view.character
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [app.interface.character-stats :refer [get-health get-max-health
                                                   experience-to-next-level]]
            [app.interface.view.attacks :refer [defender-hover-attack-view]]
            [app.interface.constant-game-data :refer [character-classes weapons]]))

(defn character-name
  [{:keys [controlled-by-player? full-name]}]
  [:span {:style {:color (if controlled-by-player? "blue" "black")}}
        full-name])

(defn- common-character-view
  [{:keys
    [experience level image controlled-by-player? has-intention? dead full-name]
    :as character}
   is-intention?]
  (let [hovered-element @(rf/subscribe [:hovered-element])
        is-hovered?     (= character hovered-element)
        next-turn-character @(rf/subscribe [:next-turn-character character])
        attacks         @(rf/subscribe [:attacks-targeting-character
                                        character])]
    (if character
      [:div {:on-mouse-over #(rf/dispatch [:hover-element
                                           :character
                                           character])
             :on-mouse-out  #()
             :style         {:z-index 2}}
       [:div.character {:style {:position   "absolute"
                                :overflow   "visible"
                                :text-align "left"
                                :top        50
                                :z-index    3
                                :display    (if (and controlled-by-player?
                                                     is-hovered?
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
        [:button.btn.btn-outline-primary
         {:on-click #()}
         "Re-equip"]
        [:button.btn.btn-outline-primary
         {:on-click #()}
         "Utility"]]
       [character-name character]
       [:br]
       [:img {:style {:opacity   (if is-intention? 0.2 1.0)
                      :transform (if dead "rotate(90deg)" nil)
                      :filter    (if (not (empty? attacks))
                                   "drop-shadow(0px 0px 20px red)"
                                   nil)}
              :src   image}]
       (if (= has-intention? is-intention?)
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
                      :left        "-20%"
                      :top         "50%"
                      :white-space "nowrap"}}
        [:div (str "Lv" level " " experience " / " experience-to-next-level)]
        [:div "↓"]
        [:div
         (str "Lv" (:level next-turn-character)
              " "   (:experience next-turn-character)
              " / " experience-to-next-level)]]
       ; Health
       [:div {:style {:position    "absolute"
                      :z-index     10
                      :left        "80%"
                      :top         "50%"
                      :white-space "nowrap"}}
        [:div (str (get-health character) " / " (get-max-health character))]
        [:div "↓"]
        [:div {:style {:color (if (:dead next-turn-character) "red" "black")}}
         (str (get-health next-turn-character)
              " / "
              (get-max-health next-turn-character))]]]
      nil)))

(defn character-view
  [character]
  (common-character-view character false))

(defn intention-character-view
  [character]
  (common-character-view character true))


(defn element-view
  [{:keys [light dark air water earth fire]}]
  ; https://stackoverflow.com/a/43958912
  [:div {:style {:display "grid"
                 :grid-gap "10px"
                 :justify-items "center"
                 :text-align "center"
                 :grid-template-columns "repeat(2, 200px)"}}
    [:div {:style {:grid-row "1"
                   :grid-column "span 2"}}
     [:b "Light " light] [:p "all-seeing, idealistic, non-interventive"]]
    [:div [:b "Air " air] [:p "impulsive, fast, carefree"]]
    [:div [:b "Water " water] [:p "meditative, redirecting"]]
    [:div [:b "Fire " fire] [:p "impulsive, powerful"]]
    [:div [:b "Earth " earth] [:p "meditative, disrupting"]]
    [:div {:style {:grid-row "4"
                   :grid-column "span 2"}}
     [:b "Dark " dark] [:p "short-sighted, pragmatic, action-oriented"]]])

; TODO show table like
;  air    |  move
;         | dodge
; earth   | block
;         |  ...
; etc.
; Turn health bar/ratio red if the character can be one shot by an enemy on
; the
; map.
(defn character-info-view
  [{:keys [class-keyword
           affinities
           weapon-levels
           image
           selected-for-chapter?
           full-name]
    :as   character}]
  [:div {:style         {:display          "grid"
                         :grid-gap         "0px"
                         :justify-items    "center"
                         :text-align       "center"
                         :background-color (if selected-for-chapter?
                                             "green"
                                             "grey")
                         :grid-template-columns "repeat(1, 300px)"}
         :on-mouse-over #(rf/dispatch [:play-animation character :attack])
         :on-mouse-out  #()
         :on-click      #(rf/dispatch [:toggle-select-for-chapter full-name])}
   [character-name character]
   [:img {:src image}]
   [:p (name class-keyword)]
   [:p (get-health character) " / " (get-max-health character) " health"]
   [:p (str weapon-levels)]
   [element-view affinities]
   #_[:table
      (into [:tbody]
            (for [[element affinity] affinities]
              [:tr [:td (name element)] [:td (str affinity)]]))]])
