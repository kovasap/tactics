(ns app.interface.initial-db
  (:require [app.interface.gridmap :refer [parse-gridmap-str]]
            [app.interface.utils :refer [associate-by]]
            [app.interface.constant-game-data :refer [character-classes]]))

; Sprites and animations can be found at
; https://github.com/wesnoth/wesnoth/tree/master/data/core/images/units
(defn make-character
  [{:keys [class-keyword] :as character-map}]
  (assoc character-map
    :image (str "class-images/" (name class-keyword) "/idle.png")
    ; Map from weapon keyword to the current weapon level.
    :weapon-levels {}
    :level 1
    :has-intention? false
    :experience 0
    :tiles-already-moved 0))

(def characters
  [(make-character
     {:full-name "Main Character"
      :letter-code "M"
      :class-keyword :skirmisher
      :controlled-by-player? true
      :equipped-weapon :sword-and-shield
      :affinities {:fire 1 :air 8 :earth 2 :water 3 :light 0 :dark 0}})
   (make-character
     {:full-name           "Opponent One"
      :letter-code         "1"
      :controlled-by-player? false
      :class-keyword       :assassin
      :ai-behavior         :attack-in-range
      :equipped-weapon     :cloak-and-dagger
      :affinities          {:fire 1 :air 3 :earth 2 :water 3 :light 0 :dark 2}})
   (make-character
     {:full-name           "Opponent Two"
      :letter-code         "2"
      :controlled-by-player? false
      :class-keyword       :assassin
      :ai-behavior         :attack-in-range
      :equipped-weapon     :cloak-and-dagger})
   (make-character
     {:full-name           "Opponent Three"
      :letter-code         "3"
      :controlled-by-player? false
      :class-keyword       :assassin
      :ai-behavior         :attack-in-range
      :equipped-weapon     :cloak-and-dagger})])

(defn parse-gridmap-str-with-characters
  [gridmap-str]
  (parse-gridmap-str gridmap-str (associate-by :letter-code characters)))

(def initial-db
  {:scenes
   [; {:title :start}
    {:title "Scenario One"
     :gridmap
     (parse-gridmap-str-with-characters
       "FM  M   M   R   F   P   P   W  
        M   W   M   R   F   P   P   W
        M   M   F   R   F   P   P   W
        M   M   M   R   F   P   P   W
        M   C   C   R   C   C   C   W
        F   M   M2  R   F1  F   F   W
        F   M   M   R   W   F   F   W
        W   M   M   W   W   W   F   W
        S   M   S   S   W   F   F   W
        S   S   S   S3  S   F   F   W
        S   S   S   S   S   F   F   W")}]
   :characters        (associate-by :full-name characters)
   :current-conversation-text "Testing!"
   :current-scene-idx 0})
