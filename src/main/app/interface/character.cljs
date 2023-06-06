(ns app.interface.character
  (:require [re-frame.core :as rf]))

(def characters
  [{:full-name "Main Character"
    :letter-code "M"
    :affinity :fire
    :stats {:move 3}
    :image "unit-images/merfolk/citizen.png"
    :animations {:attack []}}])

(defn associate-by [f coll]
  "Like groupby, but the values are single items, not lists of all matching items.
  
  Note that f must uniquely distinguish items!"
  (zipmap (map f coll) coll))

(rf/reg-sub
  :characters-by-full-name
  (fn [db _]
    (associate-by :full-name characters)))
