(ns react-native.react-native-intersection-observer
  (:require
    ["react-native-intersection-observer" :as rnio]
    [react-native.flat-list :refer [base-list-props]]
    [reagent.core :as reagent]))

(def view (reagent/adapt-react-class rnio/InView))
(def flat-list-comp (reagent/adapt-react-class rnio/IOFlatList))

(defn flat-list
  [props]
  [flat-list-comp (base-list-props props)])
