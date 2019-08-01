(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(if js/goog.DEBUG
  (.ignoreWarnings (.-YellowBox js-dependencies/react-native) #js ["re-frame: overwriting"])
  (aset js/console "disableYellowBox" true))

(defn init [app-root]
  (re-frame/dispatch [:init/app-started])
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root)))
