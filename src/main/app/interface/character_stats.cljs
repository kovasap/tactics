(ns app.interface.character-stats)

; TODO make every class give a bonus to this value
(defn get-max-health
  [character]
  (* 5 (:earth (:affinities character))))

(defn get-health
  [{:keys [health]
    :as   character}]
  (if health health (get-max-health character)))
 
