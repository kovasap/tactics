(ns app.interface.view.overworld
  (:require [re-frame.core :as rf]
            [app.interface.view.character :refer [character-info-view]]))


(defn chapter-node
  [scene-name {[x y] :location-on-map}]
  [:div {:on-mouse-over #()
         :on-mouse-out  #()
         :on-click      #(rf/dispatch [:go-to-scene scene-name])
         :style {:position "absolute"
                 :left (str x "px")
                 :top (str y "px")
                 :float "left"
                 :z-index 10}}
    [:div.dot {:style {:height "15px"
                       :width "15px"
                       :background-color "black"
                       :border-radius "50%"}}]
    [:div scene-name]])
    

(defn overworld
  []
  [:div
    ; https://azgaar.github.io/Fantasy-Map-Generator/
    (into [:div {:style {:position "relative"}}
           [:img {:src "overworld_map.svg"}]]
          (for [[scene-name scene] @(rf/subscribe [:chapter-scenes])]
            [chapter-node scene-name scene]))
    [:div "Click characters to select them for the next chapter!"]
    (into [:div] (for [character @(rf/subscribe [:party-characters])]
                   [character-info-view character]))])
