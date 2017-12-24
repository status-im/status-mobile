(ns status-im.core
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            ;[status-im.utils.config :as config]
            [reagent.core :as reagent]))

(defn init [app-root]
  ;(log/set-level! config/log-level)
  ;;(error-handler/register-exception-handler!)
  ;;(status/init-jail)
  (print "status-im.core init call")
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (re-frame/dispatch-sync [:initialize-app]))
