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
      :tiles-already-moved 0
      :affinities {:fire 1
                   :air 3
                   :earth 2
                   :water 3
                   :light 0
                   :dark 0}
      :image "unit-images/merfolk/citizen.png"
      :animations {:attack []}}]))
