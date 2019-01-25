(ns status-im.utils.publisher
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [status-im.accounts.update.publisher :as accounts]
            [status-im.contact-code.core :as contact-code]
            [status-im.utils.async :as async-util]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]))

(defonce polling-executor (atom nil))
(def sync-interval-ms 120000)
(def sync-timeout-ms  20000)

(defn- start-publisher! [web3]
  (when @polling-executor
    (async-util/async-periodic-stop! @polling-executor))
  (reset! polling-executor
          (async-util/async-periodic-exec
           (fn [done-fn]
             (let [cofx {:web3 web3
                         :now  (datetime/timestamp)
                         :db   @re-frame.db/app-db}]
               (accounts/publish-update! cofx)
               (contact-code/publish! cofx)
               (done-fn)))
           sync-interval-ms
           sync-timeout-ms)))

(re-frame/reg-fx
 ::start-publisher
 #(start-publisher! %))

(re-frame/reg-fx
 ::stop-publisher
 #(when @polling-executor
    (async-util/async-periodic-stop! @polling-executor)))

(fx/defn start-fx [{:keys [web3]}]
  {::start-publisher web3})

(fx/defn stop-fx [cofx]
  {::stop-publisher []})
