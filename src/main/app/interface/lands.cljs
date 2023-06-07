(ns app.interface.lands)

; :steps-to-move-through is used to calculate pathfindind.  A value of nil
; denotes that this terrain is impassible.

(def lands
  [{:letter     "F"
    :type       :forest
    :steps-to-move-through 1
    :perlin-cutoff 0.35
    :style      {:background-color "forestgreen"}}
   {:letter     "P"
    :type       :plains
    :steps-to-move-through 1
    :perlin-cutoff 0.3
    :style      {:background-color "orange"}}
   {:letter     "W"
    :type       :water
    :steps-to-move-through nil
    :perlin-cutoff 0.0
    :style      {:background-color "MediumTurquoise"}}
   {:letter     "M"
    :type       :mountain
    :steps-to-move-through 2
    :perlin-cutoff 0.75
    :style      {:background-color "grey"}}
   {:letter     "S"
    :type       :sand
    :steps-to-move-through 1
    :perlin-cutoff 0.2
    :style      {:background-color "yellow"}}
   {:letter     "V"
    :type       :void
    :steps-to-move-through nil
    :perlin-cutoff 10.0
    :style      {:background-color "black"}}])
