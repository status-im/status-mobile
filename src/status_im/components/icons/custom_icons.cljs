(ns status-im.components.icons.custom-icons
  (:require [reagent.core :as r]))

(def ion-icon
  (r/adapt-react-class (js/require "react-native-vector-icons/Ionicons")))

(def oct-icon
  (r/adapt-react-class (js/require "react-native-vector-icons/Octicons")))