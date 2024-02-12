(ns react-native.charts
  (:require
    ["react-native-gifted-charts" :as gifted-charts]
    [utils.reagent :as reagent]))

(def line-chart (reagent/adapt-react-class (.-LineChart gifted-charts)))
