(ns app.interface.constant-game-data)

(def weapons
  {:sword
   {:image ""
    :range 1
    :damage 2}})

(def utility-items
  {:smoke-bomb {}
   :health-potion {}})

(def character-classes
  {:skirmisher
   {:image ""
    :equippable-weapons #{:sword}
    :prerequisite-affinities {:fire 1 :earth 1}
    :equippable-utility #{:smoke-bomb :health-potion}}})
