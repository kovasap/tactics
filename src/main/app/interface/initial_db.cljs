(ns app.interface.initial-db
  (:require [app.interface.gridmap :refer [parse-gridmap-str get-tiles update-tiles]]
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

(def enemy-templates
  [(make-character
     {:full-name "Assassin"
      :letter-code "a"
      :controlled-by-player? false
      :class-keyword :assassin
      :ai-behavior :attack-in-range
      :equipped-weapon :cloak-and-dagger
      :affinities {:fire 1 :air 3 :earth 2 :water 3 :light 3 :dark 2}})
   (make-character
     {:full-name "Scholar"
      :letter-code "s"
      :controlled-by-player? false
      :class-keyword :scholar
      :ai-behavior :attack-in-range
      :equipped-weapon :tome
      :affinities {:fire 1 :air 3 :earth 2 :water 3 :light 6 :dark 1}})])

(def characters
  [(make-character
     {:full-name "Hare"
      :letter-code "H"
      :class-keyword :skirmisher
      :controlled-by-player? true
      :equipped-weapon :sword-and-shield
      :affinities {:fire 1 :air 8 :earth 2 :water 3 :light 5 :dark 0}})
   (make-character
     {:full-name "Tortoise"
      :letter-code "T"
      :class-keyword :skirmisher
      :controlled-by-player? true
      :equipped-weapon :sword-and-shield
      :affinities {:fire 1 :air 1 :earth 8 :water 3 :light 1 :dark 0}})])

(defn parse-gridmap-str-with-characters
  [gridmap-str scenario-name]
  (let [gridmap (parse-gridmap-str gridmap-str
                                   (associate-by :letter-code characters)
                                   (associate-by :letter-code
                                                 enemy-templates)
                                   scenario-name)]
    [(update-tiles gridmap #(dissoc % :generated-character))
     (map :generated-character (get-tiles gridmap :generated-character))]))


; FLAT is better than NESTED - try to keep every system in the game interacting
; with a top level key in this db.
(def initial-db
  (let
    [[scenario-1-map scenario-1-characters]
     (parse-gridmap-str-with-characters
       "M   M   M   W   FH  PT  R   R  
        M   M   F   W   F   R   R   P
        M   M   R   B   R   R   P   P
        Rs  R   R   W   W   P   P   P
        Ms  F   W   W   F   F   F   P
        Fs  F   Fa  W   Fa  F   F   F
        F   F   F   W   W   F   F   F
        F   F   F   W   W   W   F   F
        F   W   W   W   W   F   F   F
        W   W   W   W   F   F   F   F
        W   W   W   W   F   F   F   F"
       "scenario-1")]
    {:scenes         {:overworld  {}
                      :start      {}
                      :scenario-1 {:intro-dialogue
                                   [{:character-full-name "Main Character"
                                     :text "Here we can start our journey"}
                                    {:character-full-name "Main Character"
                                     :text {:fire  "Let's move!"
                                            :dark  "Let's get going!"
                                            :air   "Catch me if you can!"
                                            :earth "Give me that pack."
                                            :water "..."
                                            :light "I can see the trail"}}]
                                   :gridmap scenario-1-map
                                   :location-on-map ["500" "400"]}}
     :characters     (associate-by :full-name
                                   (concat characters scenario-1-characters))
     :dialogue-queue '()
     :current-scene  :overworld}))
