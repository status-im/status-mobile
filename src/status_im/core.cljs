(ns status-im.core
  (:require [status-im.utils.error-handler :as error-handler]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [goog.object :as object]))

(defn init [app-root]
  (log/set-level! :debug)
  ;;(error-handler/register-exception-handler!)
  (status/init-jail)
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (re-frame/dispatch-sync [:initialize-app]))
