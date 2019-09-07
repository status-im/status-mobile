(ns status-im.ui.components.webview
  (:require [reagent.core :as reagent]
            [reagent.core :as reagent.core]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            ["react-native" :as react-native]
            ["react-native-webview" :default rn-webview]))

(def webview-class
  (reagent/adapt-react-class rn-webview))

(defn module [] (.-WebViewModule ^js (.-NativeModules react-native)))

(defn webview [{:keys [dapp? dapp-name] :as opts}]
  (if (and config/cached-webviews-enabled? platform/android? dapp?)
    (reagent.core/create-class
     (let [dapp-name-sent? (reagent.core/atom false)]
       {:component-did-mount
        (fn []
          ;; unfortunately it's impossible to pass some initial params
          ;; to view, that's why we have to pass dapp-name to the module
          ;; before showing webview
          #_(.setCurrentDapp (module) dapp-name
                             (fn [] (reset! dapp-name-sent? true))))
        :reagent-render
        (fn [opts]
          (when @dapp-name-sent?
            [webview-class opts]))}))
    [webview-class opts]))
