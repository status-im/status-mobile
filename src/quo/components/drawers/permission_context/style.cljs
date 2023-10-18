(ns quo.components.drawers.permission-context.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(def radius 20)

(defn container
  []
  {:flex-direction          :row
   :background-color        (colors/theme-colors colors/white colors/neutral-90)
   :padding-top             12
   :padding-bottom          (+ 12 (safe-area/get-bottom))
   :justify-content         :center
   :padding-horizontal      20
   :shadow-offset           {:width  0
                             :height 2}
   :shadow-radius           radius
   :border-top-left-radius  radius
   :border-top-right-radius radius
   :elevation               2
   :shadow-opacity          1
   :shadow-color            colors/shadow})
