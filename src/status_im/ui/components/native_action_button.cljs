(ns status-im.ui.components.native-action-button
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(def native-action-button
  (reagent/adapt-react-class (.-default js-dependencies/action-button)))


(def native-action-button-item
  (reagent/adapt-react-class (.. js-dependencies/action-button -default -Item)))
