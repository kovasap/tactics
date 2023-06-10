(ns app.interface.view.character
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn character-name
  [{:keys [controlled-by-player? full-name]}]
  [:span {:style {:color (if controlled-by-player? "blue" "black")}}
        full-name])

(def char-hover-state (r/atom {}))
(defn- common-character-view
  [{:keys
    [full-name image controlled-by-player? has-intention? under-attack-by]
    :as character}
   is-intention?]
  (let [hover-key [full-name is-intention?]]
    (if character
      [:div {:on-mouse-over #(do (if (and controlled-by-player?
                                          (= has-intention? is-intention?))
                                   (swap! char-hover-state
                                     (fn [state] (assoc state hover-key true)))
                                   nil)
                                 (rf/dispatch [:hover-element
                                               :character
                                               character]))
             :on-mouse-out  #(swap! char-hover-state
                               (fn [state] (assoc state hover-key false)))}
       [:div
        {:style {:position   "absolute"
                 :background "white"
                 :overflow   "visible"
                 :text-align "left"
                 :top        50
                 :z-index    2
                 :display    (if (get @char-hover-state hover-key)
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
       [:br]
       (if (not (empty? under-attack-by)) [:span "under attack!"] nil)
       [:img {:style {:opacity (if is-intention? 0.2 1.0)
                      :filter  (if under-attack-by "blur(5px)" nil)}
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
(defn character-stats-view
  [{:keys [] :as character}]
  [:div
   [character-name character]])
