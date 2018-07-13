(ns status-im.thread
  (:require [cognitect.transit :as transit]
            [re-frame.db]
            [status-im.tron :as tron]))

(def rn-threads (.-Thread (js/require "react-native-threads")))

(goog-define platform "android")

(def thread (atom nil))
(def writer (transit/writer :json))
(def reader (transit/reader :json))

(def initialized? (atom false))
(def calls (atom []))

(defn post [data-str]
  (tron/log (str "Post message: " data-str))
  (.postMessage @thread data-str))

(defn make-stored-calls []
  (doseq [call @calls]
    (post call)))

(defn dispatch [args]
  (let [args' (transit/write writer args)]
    (tron/log (str "dispatch: " (first args)))
    (if @initialized?
      (post args')
      (swap! calls conj args'))))

(defn start []
  (let [new-thread (rn-threads. (str "worker." platform ".js"))]
    (set! (.-onmessage new-thread)
          (fn [data]
            (let [[event event-data :as data'] (transit/read reader data)]
              (tron/log (str "Response: " data'))

              (case event
                :initialized (do (reset! initialized? true)
                                 (make-stored-calls))
                :db (reset! re-frame.db/app-db event-data)))))
    (reset! thread new-thread)))

