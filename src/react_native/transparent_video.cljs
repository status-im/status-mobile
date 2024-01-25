(ns react-native.transparent-video
  (:require
    ["react-native-transparent-video" :default TV]
    [reagent.core :as reagent]))

(def view (reagent/adapt-react-class TV))
