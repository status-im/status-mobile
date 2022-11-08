(ns react-native.hole-view
  (:require [reagent.core :as reagent]
            ["react-native-hole-view" :refer (RNHoleView)]))

(def hole-view (reagent/adapt-react-class RNHoleView))