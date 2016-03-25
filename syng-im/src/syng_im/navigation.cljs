(ns syng-im.navigation)

(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(defn nav-pop [nav]
  (binding [*nav-render* false]
    (.pop nav)))