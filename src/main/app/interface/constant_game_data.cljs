(ns app.interface.constant-game-data)

(def weapon-advantages
  {:sword-and-shield #{:spear}
   :spear #{:axe}
   :axe #{:sword-and-shield}})

(def weapons
  {:sword-and-shield {:image "item-images/buckler.png" :range 1 :damage 6}
   :axe {:image "item-images/axe.png" :range 1 :damage 8}
   :spear {:image "item-images/spear.png" :range 1 :damage 5}
   :flail {:image "item-images/sling.png" :range 1 :damage 4}})

(def utility-items
  {:smoke-bomb {}
   :health-potion {}})

(def character-classes
  ; Images taken from wesnoth/data/core/images/units/human-peasants/peasant-
  {:skirmisher
   {:equippable-weapons #{:sword-and-shield :spear}
    :prerequisite-affinities {:fire 1 :earth 1 :air 1 :water 1}
    ; Affinites that can be leveled as this class
    :advancement-affinities {:fire 1 :earth 1}
    ; TODO automatically determine these based on the files in the
    ; resources/public/class-images/<class-name> directories
    :animation-frames {:attack 5}
    :equippable-utility #{:smoke-bomb :health-potion}}})
