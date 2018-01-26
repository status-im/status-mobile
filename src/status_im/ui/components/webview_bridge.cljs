(ns status-im.ui.components.webview-bridge
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(def webview-bridge-class
  (reagent/adapt-react-class (.-default js-dependencies/webview-bridge)))

(defn webview-bridge [opts]
  [webview-bridge-class opts])
