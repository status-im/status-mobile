(ns react-native.hole-view
  (:require
    ["react-native-hole-view" :refer (RNHoleView)]
    [reagent.core :as reagent]))

(def hole-view (reagent/adapt-react-class RNHoleView))
