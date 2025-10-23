(ns quo.components.gradient.gradient-cover.style)

(defn root-container
  [opacity height]
  {:height   (or height 252)
   :opacity  opacity
   :position :absolute
   :top      0
   :left     0
   :right    0
   :z-index  -1})
