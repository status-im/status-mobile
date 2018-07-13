(ns status-im.worker.core
  (:require
   [status-im.worker.thread]
   [status-im.worker.receiver]
   [status-im.worker.sender :as sender]))

(sender/initialized)
