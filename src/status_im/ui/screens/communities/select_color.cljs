(ns status-im.ui.screens.communities.select-color
  (:require [quo.react-native :as rn]
            ["react-native-color-wheel" :refer (ColorWheel)]
            [reagent.core :as reagent]))

(def color-wheel (reagent/adapt-react-class ColorWheel))

(defn view []
  [color-wheel
   #_{:initialColor "#ee0000"
      :onColorChang #()
      :onColorChangeComplete #()
      :style {:width 600}
      :thumbSize 2
      :thumbStyle {:height 30 :width 30 :borderRadius 30}}])