(ns status-im.worker.sender
  (:require [status-im.worker.thread :as thread]
            [cognitect.transit :as transit]
            [taoensso.timbre :as log]))

(defn post-message [message]
  (.postMessage thread/self message))

(def writer (transit/writer :json))

(defn write [data]
  (try
    (transit/write writer data)
    (catch :default e
      (log/debug :writer-error e)
      (log/debug data))))

(defn post-db [db]
  (when thread/self.onmessage
    (post-message (write [:db db]))))

(defn request-permissions [options]
  (when thread/self.onmessage
    (post-message (write [:request-permissions options]))))

(defn initialized []
  (when thread/self.onmessage
    (post-message (write [:initialized]))))
