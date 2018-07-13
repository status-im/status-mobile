(ns status-im.core
  (:require [status-im.utils.error-handler :as error-handler]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.native-module.core :as status]
            status-im.transport.impl.receive
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [goog.object :as object]
            status-im.ui.screens.signal-events
            [status-im.thread :as status-im.thread]))

(when js/goog.DEBUG
  (object/set js/console "ignoredYellowBox" #js ["re-frame: overwriting"]))

(def testfairy              (js/require "react-native-testfairy"))

(defn init [app-root]
  (log/set-level! config/log-level)
  (error-handler/register-exception-handler!)
  #_(status/init-jail)
  (when config/testfairy-enabled?
    (.begin testfairy config/testfairy-token))
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root)))
