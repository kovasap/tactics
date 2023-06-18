(ns app.interface.constant-game-data)

(def weapon-advantages
  {:sword-and-shield #{:spear}
   :spear #{:axe}
   :axe #{:sword-and-shield}})

(def weapons
  {:sword-and-shield {:image "item-images/buckler.png" :range 1 :damage 6}
   :axe {:image "item-images/axe.png" :range 1 :damage 8}
   :spear {:image "item-images/spear.png" :range 1 :damage 5}})

(def utility-items
  {:smoke-bomb {}
   :health-potion {}})

(def character-classes
  {:skirmisher
   {:image ""
    :equippable-weapons #{:sword-and-shield}
    :prerequisite-affinities {:fire 1 :earth 1}
    :equippable-utility #{:smoke-bomb :health-potion}}})
