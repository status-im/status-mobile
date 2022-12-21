(ns status-im.ui.components.slider
  (:require [reagent.core :as reagent]
            ["react-native" :refer (Animated)]
            ["@react-native-community/slider" :default Slider]))

(def slider (reagent/adapt-react-class Slider))

(def animated-slider
  (reagent/adapt-react-class (.createAnimatedComponent Animated Slider)))