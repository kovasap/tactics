(ns app.interface.initial-db
  (:require [app.interface.gridmap :refer [parse-gridmap-str]]
            [app.interface.utils :refer [associate-by]]))

; Sprites and animations can be found at
; https://github.com/wesnoth/wesnoth/tree/master/data/core/images/units
(def characters
  [{:full-name           "Main Character"
    :letter-code         "M"
    :controlled-by-player? true
    :tiles-already-moved 0
    :has-intention?      false
    :equipped-weapon     :sword
    :affinities          {:fire  1
                          :air   8
                          :earth 2
                          :water 3
                          :light 0
                          :dark  0}
    :image               "unit-images/merfolk/citizen.png"
    :animations          {:attack []}}
   {:full-name           "Opponent One"
    :letter-code         "1"
    :controlled-by-player? false
    :tiles-already-moved 0
    :equipped-weapon     :sword
    :has-intention?      false
    :affinities          {:fire  1
                          :air   3
                          :earth 2
                          :water 3
                          :light 0
                          :dark  0}
    :image               "unit-images/merfolk/entangler.png"
    :animations          {:attack []}}])

(defn parse-gridmap-str-with-characters
  [gridmap-str]
  (parse-gridmap-str gridmap-str (associate-by :letter-code characters)))

(def initial-db
  {:scenes
   [; {:title :start}
    {:gridmap
     (parse-gridmap-str-with-characters
       "FM  M   M   F   F   P   P   W  
        W   W   M   F   F   P   P   W
        W   M   F   F   F   P   P   W
        W   M   M   F   F   P   P   W
        W   M   M   F   F   F   P   W
        W   M   M   F   F1  F   F   W
        W   M   M   F   W   F   F   W
        W   M   M   W   W   W   F   W
        S   M   S   S   W   F   F   W
        S   S   S   S   S   F   F   W
        S   S   S   S   S   F   F   W")}]
   :characters        (associate-by :full-name characters)
   :current-scene-idx 0})
