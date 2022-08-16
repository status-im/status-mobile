(ns status-im.ui.components.webview
  (:require [reagent.core :as reagent]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            ["react-native-webview" :default rn-webview]))

(def webview-class
  (reagent/adapt-react-class rn-webview))

(defn webview [{:keys [dapp?] :as opts}]
  (if (and config/cached-webviews-enabled? platform/android? dapp?)
    (reagent/create-class
     (let [dapp-name-sent? (reagent/atom false)]
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
