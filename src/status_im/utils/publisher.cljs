(ns status-im.utils.publisher
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [status-im.multiaccounts.update.publisher :as multiaccounts]
            [status-im.utils.async :as async-util]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]))

(defonce polling-executor (atom nil))
(def sync-interval-ms 120000)
(def sync-timeout-ms  20000)

(defn- start-publisher! []
  (when @polling-executor
    (async-util/async-periodic-stop! @polling-executor))
  (reset! polling-executor
          (async-util/async-periodic-exec
           (fn [done-fn]
             (let [cofx {:now  (datetime/timestamp)
                         :db   @re-frame.db/app-db}]
               (multiaccounts/publish-update! cofx)
               (done-fn)))
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
  [cofx]
  {::start-publisher nil})

(fx/defn stop-fx
  [cofx]
  {::stop-publisher []})
