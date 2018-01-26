(ns status-im.ui.components.icons.custom-icons
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(def ion-icon
  (reagent/adapt-react-class (.-default js-dependencies/vector-icons)))
