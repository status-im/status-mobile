(ns legacy.status-im.ui.components.spacing)

(def spacing
  {:x-tiny   4
   :tiny     8
   :small    12
   :base     16
   :large    24
   :x-large  32
   :xx-large 48})

(def padding-horizontal
  (reduce-kv (fn [m k v]
               (assoc m k {:padding-horizontal v}))
             {}
             spacing))

(def padding-vertical
  (reduce-kv (fn [m k v]
               (assoc m k {:padding-vertical v}))
             {}
             spacing))
