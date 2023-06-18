(ns app.interface.view.scenes.start)

(defn start-scene
  []
  [:div "Starting scene!"]
  [:p "This is an intro paragraph"]
  ; https://stackoverflow.com/a/43958912
  [:div {:style {:display "grid"
                 :grid-gap "10px"
                 :justify-items "center"
                 :text-align "center"
                 :grid-template-columns "repeat(2, 200px)"}}
    [:div {:style {:grid-row "1"
                   :grid-column "span 2"}}
     [:b "Light"] [:p "all-seeing, idealistic, non-interventive"]]
    [:div [:b "Air"] [:p "impulsive, fast, carefree"]]
    [:div [:b "Water"] [:p "meditative, redirecting"]]
    [:div [:b "Fire"] [:p "impulsive, powerful"]]
    [:div [:b "Earth"] [:p "meditative, disrupting"]]
    [:div {:style {:grid-row "4"
                   :grid-column "span 2"}}
     [:b "Dark"] [:p "short-sighted, pragmatic, action-oriented"]]])
