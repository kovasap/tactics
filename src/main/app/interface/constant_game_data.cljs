(ns app.interface.constant-game-data)

(def weapon-advantages
  {:sword #{:axe}
   :spear #{:sword}
   :axe #{:spear}})

(def weapons
  {:sword {:image "" :range 1 :damage 6}
   :axe {:image "" :range 1 :damage 8}
   :spear {:image "" :range 1 :damage 5}})

(def utility-items
  {:smoke-bomb {}
   :health-potion {}})

(def character-classes
  {:skirmisher
   {:image ""
    :equippable-weapons #{:sword}
    :prerequisite-affinities {:fire 1 :earth 1}
    :equippable-utility #{:smoke-bomb :health-potion}}})
