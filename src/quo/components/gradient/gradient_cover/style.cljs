(ns quo.components.gradient.gradient-cover.style)

(defn root-container
  [opacity height]
  {:height  (or height 252)
   :opacity opacity})
