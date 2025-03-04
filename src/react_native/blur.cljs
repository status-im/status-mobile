(ns react-native.blur
  (:require
    ["@react-native-community/blur" :as blur]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(def view
  (if blur
    (reagent/adapt-react-class (.-BlurView blur))
    rn/view))
