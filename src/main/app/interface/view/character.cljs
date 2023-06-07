(ns app.interface.view.character
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(def char-hover-state (r/atom {}))
(defn- common-character-view
  [{:keys [full-name image controlled-by-player? has-intention?] :as character} is-intention?]
  (let [hover-key [full-name is-intention?]]
    (if character
      [:div {:on-mouse-over #(if (= has-intention? is-intention?)
                               (swap! char-hover-state
                                      (fn [state]
                                         (assoc state
                                          hover-key true)))
                               nil)
             :on-mouse-out  #(swap! char-hover-state (fn [state]
                                                       (assoc state
                                                         hover-key false)))}
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
        [:button.btn.btn-outline-primary {:on-click #(rf/dispatch
                                                       [:begin-attack])}
         "Attack"]]
       [:span {:style {:color (if controlled-by-player? "blue" "black")}}
        full-name
        [:img {:style {:opacity (if is-intention? 0.2 1.0)} :src image}]]]
      nil)))

(defn character-view
  [character]
  (common-character-view character false))

(defn intention-character-view
  [character]
  (common-character-view character true))
