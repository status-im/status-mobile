(ns syng-im.components.item-checkbox
  (:require [reagent.core :as r]))

(set! js/window.ItemCheckbox (js/require "react-native-circle-checkbox"))

(def item-checkbox (r/adapt-react-class js/ItemCheckbox))

