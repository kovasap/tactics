(ns app.interface.view.scenes.start)

(defn start-scene
  []
  [:div "Start!"]
  ; TODO use https://css-tricks.com/snippets/css/typewriter-effect/ and
  ; https://stackoverflow.com/questions/19986897/skip-to-end-of-css-animation
  ; to replicate scrolling text.
  [:p "This is an intro paragraph"])
