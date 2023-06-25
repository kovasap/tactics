(ns app.interface.view.conversation-overlay
  (:require [re-frame.core :as rf]))


(defn conversation-overlay
  []
  (let [{:keys [character-full-name text]} @(rf/subscribe
                                              [:current-dialogue-line])]
    [:div
     {:style {:position         "fixed" ;  Sit on top of the page content 
              :display          (if text "block" "none")
              :width            "100%" ;  Full width (cover the whole page)
                                       ; 
              :height           "100%" ;  Full height (cover the whole page)
                                       ; 
              :top              0 ;
              :left             0 ;
              :right            0 ;
              :bottom           0 ;
              :background-color "rgba(0,0,0,0.5)" ;  Black background with
                                                  ; opacity 
              :z-index          2 ;  Specify a stack order in case you're
                                  ; using a different order for other elements
                                  ; 
              :cursor           "pointer"}}
     [:div {:style {:position "absolute"
                    ; TODO make this appear in the center of the screen, or
                    ; maybe over the character that's saying it?
                    :left "500px"
                    :top "500px"
                    :z-index 100}}
       ; https://css-tricks.com/snippets/css/typewriter-effect/
       ; Could also try doing this by adding one character at a time with
       ; re-frame dispatches.
       ; TODO https://stackoverflow.com/questions/19986897/skip-to-end-of-css-animation
       ; to skip to end of text when player gets bored of animation.
       [:div character-full-name ": "
         [:div {:style {:overflow "hidden"
                        :white-space "nowrap"
                        :margin "0 auto"
                        :animation "typing 3.5s steps(40, end)"}}
          text]]
       [:button.btn.btn-outline-primary {:on-click #(rf/dispatch
                                                      [:advance-dialogue])}
        "Advance dialogue"]]]))

