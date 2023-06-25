(ns app.interface.view.gridmap
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as st]
            [app.interface.view.character
             :refer
             [character-view intention-character-view]]
            [app.interface.config :refer [debug]]
            [app.interface.utils :refer [get-only]]))


(def tile-size "120px")


(defn tile-view
  [{:keys [land
           row-idx
           col-idx
           character-full-name
           is-legal-move
           is-legal-attack
           waypoint-for
           intention-character-full-name]
    :as   tile}]
  (let [character (get @(rf/subscribe [:characters-by-full-name])
                       character-full-name)
        intention-character (get @(rf/subscribe [:characters-by-full-name])
                                 intention-character-full-name)]
    [:div.tile
     {:style         {:font-size    "12px"
                      :text-align   "center"
                      :height       tile-size
                      :width        tile-size
                      :aspect-ratio "1"
                      :position     "relative"}
      :on-mouse-over #(do (when (not intention-character-full-name) ; why?
                            (rf/dispatch [:hover-element :tile tile]))
                          (when is-legal-move
                           (rf/dispatch [:preview-move-intention tile])))
      :on-mouse-out  #()
      :on-click      #(cond is-legal-move   (rf/dispatch
                                              [:declare-move-intention tile]))}
                            ; Moving into another characters range
                            ; automatically fights if the other character
                            ; sticks around
                            ; is-legal-attack (rf/dispatch
                            ;                   [:declare-attack-intention
                            ;                    character-full-name]))}
     [:div.background
      {:style (merge (:style land)
                     {:width        "100%"
                      :height       "100%"
                      :position     "absolute"
                      :z-index      -1
                      ; Moving into another characters range automatically
                      ; fights if the other character sticks around
                      ; :border-style (if is-legal-attack "solid" nil)
                      ; :border-color (if is-legal-attack "darkred" nil)
                      :opacity      (if is-legal-move 0.9 0.7)})}]
     [:div {:style {:position "absolute" :padding-top "10px" :width "100%"}}
      [:div {:style {:display (if debug "block" "none")}}
       row-idx
       ", "
       col-idx]
      (if waypoint-for [:span "wp"] nil)
      [character-view character]
      (if (not (and character-full-name intention-character-full-name))
        [intention-character-view intention-character]
        nil)]]))


(defn tile-info-view
  [{:keys [row-idx col-idx] {:keys [terrain steps-to-move-through]} :land}]
  [:div
   [:p row-idx ", " col-idx]
   [:p (name terrain)]
   [:p (if (nil? steps-to-move-through)
         "impassible"
         (str steps-to-move-through " steps to traverse"))]])


(defn gridmap-view
  [gridmap]
  (into [:div.gridmap
         {:style {:display  "grid"
                  :grid-template-columns (st/join " "
                                                  (for [_ (first gridmap)]
                                                    tile-size))
                  :grid-gap "1px"}}]
        (reduce concat
          (for [column gridmap] (for [tile column] (tile-view tile))))))
