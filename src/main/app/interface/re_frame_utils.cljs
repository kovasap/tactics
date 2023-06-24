(ns app.interface.re-frame-utils)

(def buffer-ms 10)

(defn dispatch-sequentially-with-timings
  [events-with-timings]
  (into []
        (for [[i [event timing]] (map-indexed vector events-with-timings)]
          [:dispatch-later
           {:ms       (reduce +
                        (map #(+ buffer-ms (last %))
                          (subvec (vec events-with-timings) 0 (inc i))))
            :dispatch event}])))
