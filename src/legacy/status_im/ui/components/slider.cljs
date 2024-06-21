(ns legacy.status-im.ui.components.slider
  (:require
    ["@react-native-community/slider" :default Slider]
    ["react-native" :refer (Animated)]
    [reagent.core :as reagent]))

(def slider (reagent/adapt-react-class Slider))

(def animated-slider
  (reagent/adapt-react-class (.createAnimatedComponent Animated Slider)))
