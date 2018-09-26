(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.error-handler :as error-handler]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            status-im.transport.impl.receive
            status-im.transport.impl.send
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [goog.object :as object]
            cljs.core.specs.alpha))

(when js/goog.DEBUG
  (.ignoreWarnings (.-YellowBox js-dependencies/react-native) #js ["re-frame: overwriting"]))

(defn init [app-root]
  (log/set-level! config/log-level)
  (error-handler/register-exception-handler!)
  (re-frame/dispatch [:init/app-started])
  (when config/testfairy-enabled?
    (.begin js-dependencies/testfairy config/testfairy-token))
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root)))
