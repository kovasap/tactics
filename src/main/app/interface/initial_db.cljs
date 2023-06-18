(ns app.interface.initial-db
  (:require [app.interface.gridmap :refer [parse-gridmap-str]]
            [app.interface.utils :refer [associate-by]]
            [app.interface.constant-game-data :refer [character-classes]]))

; Sprites and animations can be found at
; https://github.com/wesnoth/wesnoth/tree/master/data/core/images/units
(defn make-character
  [{:keys [class-keyword] :as character-map}]
  (assoc character-map
    :image (str "unit-images/" (name class-keyword) "idle.png")
    ; Map from weapon keyword to the current weapon level.
    :weapon-levels {}
    :level 1
    :experiece 0
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
      :class-keyword       :skirmisher
      :equipped-weapon     :spear
      :affinities          {:fire 1 :air 3 :earth 2 :water 3 :light 0 :dark 0}})])

(defn parse-gridmap-str-with-characters
  [gridmap-str]
  (parse-gridmap-str gridmap-str (associate-by :letter-code characters)))

(def initial-db
  {:scenes
   [; {:title :start}
    {:title "Scenario One"
     :gridmap
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
   :current-conversation-text "Testing!"
   :current-scene-idx 0})
