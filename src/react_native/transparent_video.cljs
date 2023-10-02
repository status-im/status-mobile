(ns react-native.transparent-video
  (:require [reagent.core :as reagent]
            ["react-native-transparent-video" :default TV]))

(def view (reagent/adapt-react-class TV))
