(ns status-im.worker.core
  (:require
   [status-im.worker.thread]
   [status-im.worker.receiver]
   [status-im.worker.sender :as sender]
   [taoensso.timbre :as log]
   [status-im.utils.config :as config]
   [status-im.native-module.core :as status]))

(defn init []
  (log/set-level! config/log-level)
  (status/init-jail)
  (sender/initialized))
