(ns quo.components.avatars.token-avatar.style)

(def ^:const size 32)

(def container
  {:height size
   :width  size})

(def hole-view
  {:width  size
   :height size})

(def context
  {:width         16
   :height        16
   :border-radius 8
   :position      :absolute
   :right         -4
   :bottom        -4})

(defn image
  [type]
  {:width         size
   :border-radius (if (= type :collectible) 8 0)
   :height        size})
