(ns quo.components.wallet.network-link.helpers)

(def ^:private central-figure-width 63)

(defn calculate-side-lines-path-1x
  "Calculates the `d` attribute for the side lines based on the SVG width."
  [width]
  (let [side-offset (/ (- width central-figure-width) 2)]
    {:left  (str "M0 57 L" side-offset " 57")
     :right (str "M" (+ side-offset central-figure-width) " 1 L" width " 1")}))

(defn calculate-transform
  "Calculates the transform attribute for the central figure based on the SVG width."
  [width]
  (let [translate-x (/ (- width central-figure-width) 2)]
    (str "translate(" translate-x " 0)")))

(defn calculate-side-lines-path-2x
  "Calculates the `d` attribute for the side lines based on the SVG width."
  [width]
  (let [side-offset (/ (- width central-figure-width) 2)]
    {:left  (str "M0 113 L" side-offset " 113")
     :right (str "M" (+ side-offset central-figure-width) " 1 L" width " 1")}))
