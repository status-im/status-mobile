(ns syng-im.navigation)

(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(defn nav-pop [nav]
  (binding [*nav-render* true]
    (.pop nav)))

(defn nav-push [nav route]
  (binding [*nav-render* true]
    (.push nav (clj->js route))))

(defn nav-replace [nav route]
  (binding [*nav-render* true]
    (.replace nav (clj->js route))))
