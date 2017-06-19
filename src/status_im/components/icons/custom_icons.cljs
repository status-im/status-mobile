(ns status-im.components.icons.custom-icons
  (:require [reagent.core :as r]))

(def ion-icon
  (r/adapt-react-class (.-default (js/require "react-native-vector-icons/Ionicons"))))
