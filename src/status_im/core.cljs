(ns status-im.core
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.native-module.core :as status]
            [reagent.core :as reagent]))

(defn init [app-root]
  (log/set-level! :debug)
  ;;(error-handler/register-exception-handler!)
  ;;(status/init-jail)
  (print "status-im.core init call")
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (re-frame/dispatch-sync [:initialize-app]))
