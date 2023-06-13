(ns app.interface.view.character
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [app.interface.character-stats :refer [get-health get-max-health]]))

(defn character-name
  [{:keys [controlled-by-player? full-name]}]
  [:span {:style {:color (if controlled-by-player? "blue" "black")}}
        full-name])

(defn- common-character-view
  [{:keys [image controlled-by-player? has-move-intention? dead] :as character}
   is-intention?]
  (let [hovered-element @(rf/subscribe [:hovered-element])
        is-hovered?     (= character hovered-element)
        under-attack?   (and @(rf/subscribe [:under-attack? character])
                             (= (boolean has-move-intention?) is-intention?))]
    (if character
      [:div {:on-mouse-over #(rf/dispatch [:hover-element
                                           :character
                                           character])
             :on-mouse-out  #()
             :style         {:z-index 2}}
       [:div
        {:style {:position   "absolute"
                 :background "white"
                 :overflow   "visible"
                 :text-align "left"
                 :top        50
                 :z-index    3
                 :display    (if (and controlled-by-player?
                                      is-hovered?
                                      (not dead)
                                      (= (boolean has-move-intention?)
                                         is-intention?))
                               "block"
                               "none")}}
        [:button.btn.btn-outline-primary
         {:on-click #(if @(rf/subscribe [:moving-character])
                       (rf/dispatch [:cancel-move])
                       (rf/dispatch [:begin-move character]))}
         "Move"]
        [:button.btn.btn-outline-primary
         {:on-click #(if @(rf/subscribe [:attacking-character])
                       (rf/dispatch [:cancel-attack])
                       (rf/dispatch [:begin-attack character]))}
         "Attack"]]
       [character-name character]
       [:progress.health {:value (get-health character)
                          :max   (get-max-health character)}]
       [:br]
       (if under-attack? [:span "under attack!"] nil)
       [:img {:style {:opacity (if is-intention? 0.2 1.0)
                      :transform (if dead "rotate(90deg)" nil)
                      :filter  (if under-attack?
                                 "drop-shadow(0px 0px 20px red)"
                                 nil)}
              :src   image}]]
      nil)))

(defn character-view
  [character]
  (common-character-view character false))

(defn intention-character-view
  [character]
  (common-character-view character true))

; TODO show table like
;  air    |  move
;         | dodge
; earth   | block
;         |  ...
; etc.
; Turn health bar/ratio red if the character can be one shot by an enemy on the
; map.
(defn character-info-view
  [{:keys [character-class affinities] :as character}]
  [:div
   [character-name character]
   [:p (name character-class)]
   [:p (get-health character) " / " (get-max-health character)]
   [:table
     (into [:tbody]
           (for [[element affinity] affinities]
             [:tr [:td (name element)] [:td (str affinity)]]))]])
