(ns status-im.components.webview-bridge
  (:require [reagent.core :as r]))

(def webview-bridge-class
  (r/adapt-react-class (.-default (js/require "react-native-webview-bridge"))))

(defn webview-bridge [opts]
  [webview-bridge-class opts])
