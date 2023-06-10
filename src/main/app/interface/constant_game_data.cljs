(ns app.interface.constant-game-data)

(def weapons
  {:sword
   {:image ""
    :range 1
    :damage 2}})

(def utility-items
  {:smoke-bomb {}
   :health-potion {}})

(def classes
  {:skirmisher
   {:image ""
    :equippable-weapons #{:sword}
    :equippable-utility #{:smoke-bomb :health-potion}}})
