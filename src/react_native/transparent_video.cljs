(ns react-native.transparent-video
  (:require
    ["react-native-transparent-video" :default TV]
    [utils.reagent :as reagent]))

(def view (reagent/adapt-react-class TV))
