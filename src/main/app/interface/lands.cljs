(ns app.interface.lands)

; :steps-to-move-through is used to calculate pathfindind.  A value of nil
; denotes that this terrain is impassible.

(def lands
  [{:letter        "F"
    :terrain       :forest
    :steps-to-move-through 1
    :perlin-cutoff 0.35
    :aspects {:fire 0 :air 1 :earth 1 :water 1 :light 0 :dark 1}
    :style         {:background-color "forestgreen"}}
   {:letter        "P"
    :terrain       :plains
    :steps-to-move-through 1
    :perlin-cutoff 0.3
    :aspects {:fire 1 :air 1 :earth 1 :water 1 :light 1 :dark 0}
    :style         {:background-color "orange"}}
   {:letter        "W"
    :terrain       :water
    :steps-to-move-through nil
    :perlin-cutoff 0.0
    :aspects {:fire 0 :air 0 :earth 0 :water 3 :light 0 :dark 0}
    :style         {:background-color "MediumTurquoise"}}
   {:letter        "M"
    :terrain       :mountain
    :steps-to-move-through 2
    :perlin-cutoff 0.75
    :aspects {:fire 0 :air 1 :earth 2 :water 0 :light 0 :dark 0}
    :style         {:background-color "grey"}}
   {:letter        "S"
    :terrain       :sand
    :steps-to-move-through 1
    :perlin-cutoff 0.2
    :aspects {:fire 2 :air 1 :earth 0 :water 0 :light 1 :dark 0}
    :style         {:background-color "yellow"}}
   {:letter        "C"
    :terrain       :wall
    :steps-to-move-through nil
    :aspects {:fire 0 :air 0 :earth 2 :water 0 :light 0 :dark 1}
    :style         {:background-color "DimGrey"}}
   {:letter        "R"
    :terrain       :road
    :steps-to-move-through 1
    :aspects {:fire 1 :air 0 :earth 1 :water 0 :light 1 :dark 0}
    :style         {:background-color "AntiqueWhite"}}
   {:letter        "V"
    :terrain       :void
    :steps-to-move-through nil
    :perlin-cutoff 10.0
    :aspects {:fire 0 :air 0 :earth 0 :water 0 :light 0 :dark 3}
    :style         {:background-color "black"}}])
