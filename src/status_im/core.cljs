(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im.notifications.background :as background-messaging]
            [reagent.core :as reagent]
            status-im.transport.impl.receive
            status-im.transport.impl.send
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [goog.object :as object]
            cljs.core.specs.alpha))

(if js/goog.DEBUG
  (.ignoreWarnings (.-YellowBox js-dependencies/react-native) #js ["re-frame: overwriting"])
  (aset js/console "disableYellowBox" true))

(defn init [app-root]
  (log/set-level! config/log-level)
  (error-handler/register-exception-handler!)
  (re-frame/dispatch [:init/app-started])
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (when platform/android?
    (.registerHeadlessTask react/app-registry "RNFirebaseBackgroundMessage" background-messaging/message-handler-fn)))
