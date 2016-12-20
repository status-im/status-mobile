(ns status-im.components.webview-bridge
  (:require [status-im.utils.utils :as u]
            [reagent.core :as r]
            [status-im.utils.platform :as p]))

(def webview-bridge-class
  (r/adapt-react-class (.-default (js/require "react-native-webview-bridge"))))

(defn webview-bridge [opts]
  [webview-bridge-class opts])
