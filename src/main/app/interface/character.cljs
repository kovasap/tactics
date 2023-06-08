(ns app.interface.character
  (:require [re-frame.core :as rf]
            [app.interface.utils :refer [associate-by]]))

(def weapons
  {:sword
   {:image ""
    :range 1
    :damage 2}})

; Sprites and animations can be found at
; https://github.com/wesnoth/wesnoth/tree/master/data/core/images/units

(def starting-characters
  (associate-by :full-name
    [{:full-name "Main Character"
      :letter-code "M"
      :controlled-by-player? true
      :tiles-already-moved 0
      :has-intention? false
      :equipped-weapon :sword
      :affinities {:fire 1
                   :air 8
                   :earth 2
                   :water 3
                   :light 0
                   :dark 0}
      :image "unit-images/merfolk/citizen.png"
      :animations {:attack []}}
     {:full-name "Opponent One"
      :letter-code "1"
      :controlled-by-player? false
      :tiles-already-moved 0
      :equipped-weapon :sword
      :has-intention? false
      :affinities {:fire 1
                   :air 3
                   :earth 2
                   :water 3
                   :light 0
                   :dark 0}
      :image "unit-images/merfolk/entangler.png"
      :animations {:attack []}}]))

(rf/reg-sub
 :characters-by-full-name
 (fn [db _]
   (:characters db)))
