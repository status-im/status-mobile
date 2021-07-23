(ns status-im.ui.screens.communities.select-color
  (:require [quo.react-native :as rn]
            ["react-native-color-wheel" :refer (ColorWheel)]
            [reagent.core :as reagent]
            [quo.core :as quo]))

(def color-wheel (reagent/adapt-react-class ColorWheel))

(defn view []
  [rn/view {:flex 1}
   [color-wheel
    {:initialColor "#ee0000"
     :onColorChang #()
     :onColorChangeComplete #(println %)}]
   [quo/text-input
    {:container-style {:margin-bottom 100
                       :margin-horizontal 48}
     :on-change-text #()}]])