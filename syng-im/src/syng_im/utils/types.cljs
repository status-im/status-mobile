(ns syng-im.utils.types)

(defn to-string [s]
  (if (keyword? s)
    (name s)
    s))

(defn to-edn-string [value]
  (with-out-str (pr value)))