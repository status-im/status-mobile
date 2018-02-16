(ns status-im.core
  (:require [status-im.utils.error-handler :as error-handler]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [goog.object :as object]))

(when js/goog.DEBUG
  (object/set js/console "ignoredYellowBox" #js ["re-frame: overwriting"]))

(defn init [app-root]
  #_(log/debug "[INIT] log/set-level!")
  #_(log/set-level! config/log-level)
  (log/debug "[INIT] error-handler/register-exception-handler!")
  (error-handler/register-exception-handler!)
  (log/debug "[INIT] .registerComponent")
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (log/debug "[INIT] re-frame/dispatch-sync :initialize-app")
  (re-frame/dispatch-sync [:initialize-app]))
