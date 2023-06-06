(ns app.interface.view.character)

(defn- common-character-view
  [{:keys [full-name image controlled-by-player?] :as character} is-intention?]
  (if character
    [:div
     [:span {:style {:color (if controlled-by-player? "blue" "black")}}
      full-name]
     [:img {:style {:opacity (if is-intention? 0.2 1.0)}
            :src image}]]
    nil))

(defn character-view
  [character]
  (common-character-view character false))

(defn intention-character-view
  [character]
  (common-character-view character true))
