(ns quo2.components.graph.wallet-graph.utils)

(defn find-highest-value
  [coll]
  (apply max (map :value coll)))

(defn downsample-data
  [data max-array-size]
  (let [data-size (count data)]
    (if (> data-size max-array-size)
      (let [step-size (max (/ data-size max-array-size) 1)]
        (vec (take-nth step-size data)))
      data)))
