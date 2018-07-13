(ns status-im.worker.receiver
  (:require
   [status-im.worker.thread :as thread]
   [cognitect.transit :as transit]
   [re-frame.core :as re-frame]
   [status-im.tron :as tron]
   [status-im.ui.screens.events]))

(def reader (transit/reader :json))

(defn on-message [message-str]
  (tron/log (str "message received: " message-str))
  (re-frame/dispatch (transit/read reader message-str)))


(set! (.-onmessage thread/self) on-message)
