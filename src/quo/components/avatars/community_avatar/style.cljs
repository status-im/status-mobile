(ns quo.components.avatars.community-avatar.style)

(def ^:private sizes
  {:size-32 32
   :size-24 24})

(defn image
  [size]
  (let [size-val (sizes size)]
    {:border-radius (/ size-val 2)
     :width         size-val
     :height        size-val}))
