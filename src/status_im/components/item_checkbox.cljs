(ns status-im.components.item-checkbox
  (:require [reagent.core :as r]))

(def item-checkbox (r/adapt-react-class (js/require "react-native-circle-checkbox")))

