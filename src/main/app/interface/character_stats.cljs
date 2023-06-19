(ns app.interface.character-stats 
  (:require
    [re-frame.core :as rf]))

; TODO on leveling, have each character gain levels / experience in each
; affinity proportional to the number of tiles of that land type they traversed
; and faught on since their last level

; TODO make every class give a bonus to this value
(defn get-max-health
  [character]
  (* 5 (:earth (:affinities character))))

(defn get-health
  [{:keys [health]
    :as   character}]
  (if health health (get-max-health character)))

(def experience-to-next-level 100)
