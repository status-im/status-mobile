(ns react-native.linear-gradient
  (:require ["react-native-linear-gradient" :default LinearGradient]
            [reagent.core :as reagent]))

(def linear-gradient (reagent/adapt-react-class LinearGradient))