(ns app.interface.view.conversation-overlay
  (:require [re-frame.core :as rf]))


(defn conversation-overlay
  []
  (let [current-text @(rf/subscribe :current-conversation-text)]
    [:div {:style {"position"         "fixed" ; /* Sit on top of the page
                                              ; content */
                   "display"          (if current-text "block" "none")
                   "width"            "100%" ; /* Full width (cover the whole
                                             ; page) */
                   "height"           "100%" ; /* Full height (cover the whole
                                             ; page) */
                   "top"              0 ;
                   "left"             0 ;
                   "right"            0 ;
                   "bottom"           0 ;
                   "background-color" "rgba(0,0,0,0.5)" ; /* Black background
                                                        ; with opacity */
                   "z-index"          2 ; /* Specify a stack order in case
                                        ; you're using a different order for
                                        ; other elements */
                   "cursor"           "pointer"}}
    ; TODO use https://css-tricks.com/snippets/css/typewriter-effect/ and
    ; https://stackoverflow.com/questions/19986897/skip-to-end-of-css-animation
    ; to replicate scrolling text.
     [:div current-text]])); /* Add a pointer on hover */

