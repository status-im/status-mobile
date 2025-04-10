(ns quo.components.drawers.permission-context.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]))

(def ^:private radius 20)

(def blur-container
  {:background-color (when platform/ios? :transparent)
   :position         :absolute
   :top              0
   :left             0
   :right            0
   :bottom           0})

(defn container
  [blur? theme]
  {:flex-direction          :row
   :overflow                :hidden
   :background-color        (when-not blur?
                              (colors/theme-colors colors/white
                                                   colors/neutral-95
                                                   theme))
   :padding-top             12
   :padding-bottom          (+ 12 safe-area/bottom)
   :justify-content         :center
   :padding-horizontal      20
   :border-top-left-radius  radius
   :border-top-right-radius radius})

(def token-group
  {:flex-direction :row
   :margin-left    3})
