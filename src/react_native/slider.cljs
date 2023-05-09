(ns react-native.slider
  (:require ["@react-native-community/slider" :default Slider]
            [reagent.core :as reagent]))

(def slider (reagent/adapt-react-class Slider))
