(ns react-native.slider
  (:require
    ["@react-native-community/slider" :default Slider]
    [utils.reagent :as reagent]))

(def slider (reagent/adapt-react-class Slider))
