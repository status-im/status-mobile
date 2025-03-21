(ns utils.vector)

(defn insert-element-at
  [data element index]
  (let [before (take index data)
        after  (drop index data)]
    (vec (concat before [element] after))))
