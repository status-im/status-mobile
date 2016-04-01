(ns syng-im.components.icons.ionicons
  (:require [reagent.core :as r]))

(set! js/window.Ionicons (js/require "react-native-vector-icons/Ionicons"))

(def icon (r/adapt-react-class js/Ionicons))
