(ns legacy.status-im.ui.components.webview
  (:require
    ["react-native-webview" :default rn-webview]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [status-im2.config :as config]))

(def webview-class
  (reagent/adapt-react-class rn-webview))

(defn webview
  [{:keys [dapp?] :as opts}]
  (if (and config/cached-webviews-enabled? platform/android? dapp?)
    (reagent/create-class
     (let [dapp-name-sent? (reagent/atom false)]
       {:component-did-mount
        (fn []
          ;; unfortunately it's impossible to pass some initial params
          ;; to view, that's why we have to pass dapp-name to the module
          ;; before showing webview
          #_(.setCurrentDapp (module)
                             dapp-name
                             (fn [] (reset! dapp-name-sent? true))))
        :reagent-render
        (fn [opts]
          (when @dapp-name-sent?
            [webview-class opts]))}))
    [webview-class opts]))
