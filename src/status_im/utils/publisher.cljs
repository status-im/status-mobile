(ns status-im.utils.publisher
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [status-im.utils.async :as async-util]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.fx :as fx]))

(defonce polling-executor (atom nil))
(def sync-interval-ms 10000)
(def sync-timeout-ms  20000)

(defn- start-publisher! []
  (when @polling-executor
    (async-util/async-periodic-stop! @polling-executor))
  (reset! polling-executor
          (async-util/async-periodic-exec
           (fn [done-fn]
             (mailserver/check-connection!)
             (done-fn))
           sync-interval-ms
           sync-timeout-ms)))

(re-frame/reg-fx
 ::start-publisher
 start-publisher!)

(re-frame/reg-fx
 ::stop-publisher
 #(when @polling-executor
    (async-util/async-periodic-stop! @polling-executor)))

(fx/defn start-fx
  [_]
  {::start-publisher nil})

(fx/defn stop-fx
  [_]
  {::stop-publisher []})
