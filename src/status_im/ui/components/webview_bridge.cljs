(ns status-im.ui.components.webview-bridge
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [reagent.core :as reagent.core]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]))

(def webview-bridge-class (reagent/adapt-react-class (.-WebView js-dependencies/webview-bridge)))

(defn webview-bridge [{:keys [dapp? dapp-name] :as opts}]
  [webview-bridge-class opts])
