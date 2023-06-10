(ns app.interface.lands)

; :steps-to-move-through is used to calculate pathfindind.  A value of nil
; denotes that this terrain is impassible.

(def lands
  [{:letter        "F"
    :terrain       :forest
    :steps-to-move-through 1
    :perlin-cutoff 0.35
    :style         {:background-color "forestgreen"}}
   {:letter        "P"
    :terrain       :plains
    :steps-to-move-through 1
    :perlin-cutoff 0.3
    :style         {:background-color "orange"}}
   {:letter        "W"
    :terrain       :water
    :steps-to-move-through nil
    :perlin-cutoff 0.0
    :style         {:background-color "MediumTurquoise"}}
   {:letter        "M"
    :terrain       :mountain
    :steps-to-move-through 2
    :perlin-cutoff 0.75
    :style         {:background-color "grey"}}
   {:letter        "S"
    :terrain       :sand
    :steps-to-move-through 1
    :perlin-cutoff 0.2
    :style         {:background-color "yellow"}}
   {:letter        "V"
    :terrain       :void
    :steps-to-move-through nil
    :perlin-cutoff 10.0
    :style         {:background-color "black"}}])
