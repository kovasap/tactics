(ns app.interface.view.character)

(defn character-view
  [{:keys [full-name image]}]
  [:div
   [:span full-name]
   [:img {:src image}]])
