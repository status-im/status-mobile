(ns react-native.blur
  (:require ["@react-native-community/blur" :as blur]
            [reagent.core :as reagent]))

(def view (reagent/adapt-react-class (.-BlurView blur)))
