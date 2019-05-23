(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im.notifications.background :as background-messaging]
            [reagent.core :as reagent]
            status-im.transport.impl.receive
            status-im.transport.impl.send
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.logging.core :as utils.logs]
            [goog.object :as object]
            cljs.core.specs.alpha))

(if js/goog.DEBUG
  (.ignoreWarnings (.-YellowBox js-dependencies/react-native) #js ["re-frame: overwriting"])
  (aset js/console "disableYellowBox" true))

(defn init [app-root]
  (utils.logs/init-logs)
  (error-handler/register-exception-handler!)
  (re-frame/dispatch [:init/app-started])
  ((object/get react/app-registry "registerComponent") "StatusIm" #(reagent/reactify-component app-root))
  (when platform/android?
    ((object/get react/app-registry "registerHeadlessTask") "RNFirebaseBackgroundMessage" background-messaging/message-handler-fn)))
