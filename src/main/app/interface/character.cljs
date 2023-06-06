(ns app.interface.character
  (:require [re-frame.core :as rf]
            [app.interface.utils :refer [associate-by]]))

; Sprites and animations can be found at
; https://github.com/wesnoth/wesnoth/tree/master/data/core/images/units

(def starting-characters
  (associate-by :full-name
    [{:full-name "Main Character"
      :letter-code "M"
      :controlled-by-player? true
      :already-moved? false
      :affinity :fire
      :stats {:move 3}
      :image "unit-images/merfolk/citizen.png"
      :animations {:attack []}}]))
