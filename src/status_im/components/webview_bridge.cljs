(ns status-im.components.webview-bridge
  (:require [status-im.utils.utils :as u]
            [reagent.core :as r]))

(def webview-bridge-class
  (r/adapt-react-class (js/require "react-native-webview-bridge")))

(defn webview-bridge [opts]
  [webview-bridge-class opts])
