(ns syng-im.components.nav)

(def ^{:dynamic true :private true} *nav-render*
  "Flag to suppress navigator re-renders from outside om when pushing/popping."
  true)

(defn nav-push [nav route]
  (binding [*nav-render* false]
    (.push nav (clj->js route))))

(defn nav-replace [nav route]
  (binding [*nav-render* false]
    (.replace nav (clj->js route))))

(defn nav-pop [nav]
  (binding [*nav-render* false]
    (.pop nav)))
