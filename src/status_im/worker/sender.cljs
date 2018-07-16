(ns status-im.worker.sender
  (:require [status-im.worker.thread :as thread]
            [cognitect.transit :as transit]))

(defn post-message [message]
  (.postMessage thread/self message))

(def writer (transit/writer :json))

(defn post-db [db]
  (when thread/self.onmessage
    (post-message (transit/write writer [:db db]))))

(defn initialized []
  (when thread/self.onmessage
    (post-message (transit/write writer [:initialized]))))
