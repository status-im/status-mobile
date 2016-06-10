(ns status-im.components.icons.ionicons
  (:require [reagent.core :as r]))

(def icon (r/adapt-react-class (js/require "react-native-vector-icons/Ionicons")))
