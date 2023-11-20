(ns react-native.react-native-intersection-observer
  (:require
    ["react-native-intersection-observer" :refer [InView IOFlatList]]
    [react-native.flat-list :refer [base-list-props]]
    [reagent.core :as reagent]))

(def view (reagent/adapt-react-class InView))
(def flat-list-comp (reagent/adapt-react-class IOFlatList))

(defn flat-list
  [props]
  [flat-list-comp (base-list-props props)])
