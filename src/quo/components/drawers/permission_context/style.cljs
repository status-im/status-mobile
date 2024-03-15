(ns quo.components.drawers.permission-context.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(def ^:private radius 20)

(defn container
  [blur? theme]
  {:flex-direction          :row
   :background-color        (if blur?
                              (colors/theme-colors colors/white-70-blur
                                                   colors/neutral-95-opa-70-blur
                                                   theme)
                              (colors/theme-colors colors/white
                                                   colors/neutral-95
                                                   theme))
   :padding-top             12
   :padding-bottom          (+ 12 (safe-area/get-bottom))
   :justify-content         :center
   :padding-horizontal      20
   :border-top-left-radius  radius
   :border-top-right-radius radius
   :shadow-offset           {:width  0
                             :height 2}
   :shadow-radius           radius
   :elevation               2
   :shadow-opacity          1
   :shadow-color            colors/shadow})
