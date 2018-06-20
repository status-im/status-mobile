(ns status-im.core
  (:require [status-im.utils.error-handler :as error-handler]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.native-module.core :as status]
            status-im.transport.impl.receive
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [goog.object :as object]))

(when js/goog.DEBUG
  (object/set js/console "ignoredYellowBox" #js ["re-frame: overwriting"]))

(defn init [app-root]
  (log/set-level! config/log-level)
  (error-handler/register-exception-handler!)
  (status/init-jail)
  (when config/testfairy-enabled?
    (.begin js-dependencies/testfairy config/testfairy-token))
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root)))
