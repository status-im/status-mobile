(ns messenger.utils.types)

(defn to-string [s]
  (if (keyword? s)
    (name s)
    s))
