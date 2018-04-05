(ns status-im.network.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.network.net-info :as net-info]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [status-im.constants :as constants]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.utils :as utils]))

(re-frame/reg-fx
  ::listen-to-network-status
  (fn [[connection-listener net-info-listener]]
    (net-info/is-connected? connection-listener)
    (net-info/net-info net-info-listener)
    (net-info/add-connection-listener connection-listener)
    (net-info/add-net-info-listener net-info-listener)))

(re-frame/reg-fx
  ::notify-status-go
  (fn [data]
    (status/connection-change data)))

(re-frame/reg-fx
  :check-connection-later
  (fn [{:keys [timeout callback]}]
    (utils/set-timeout
     #(net-info/is-connected? callback)
     timeout)))


(handlers/register-handler-fx
 :listen-to-network-status
 (fn []
   {::listen-to-network-status [#(re-frame/dispatch [::update-connection-status %])
                                #(re-frame/dispatch [::update-network-status %])]}))

(handlers/register-handler-fx
 ::update-connection-status
 [re-frame/trim-v (re-frame/inject-cofx :now-s)]
 (fn [{:keys [db now-s]} [connected?]]
   (let [{:network-status/keys [offline-timestamp]
          :app-state/keys      [state active-timestamp background-timestamp]
          :keys                [web3]} db
         from      (if (and background-timestamp
                            (< background-timestamp offline-timestamp active-timestamp))
                     background-timestamp
                     offline-timestamp)
         time-diff (if from (- now-s from) 0)
         active?   (= state :active)]
     (log/info "Update connection status"
               {:is-connected                            connected?
                :off-on-time-diff                        time-diff
                :app-state                               state
                :offline-timestamp                       offline-timestamp
                :background-timestamp                    background-timestamp
                :active-timestamp                        active-timestamp
                "(> active-timestamp offline-timestamp)" (> active-timestamp offline-timestamp)})
     (cond->
      {:db (cond-> (assoc db :network-status (if connected? :online :offline))

                   (and (not connected?) active?)
                   (assoc :network-status/offline-timestamp now-s)

                   connected?
                   (dissoc :network-status/offline-timestamp))}

      (and connected?
           active?
           (> time-diff constants/history-requesting-threshold-seconds))
      (merge (let [from' (datetime/minute-before from)]
               {:dispatch [:initialize-offline-inbox web3 from' now-s]}))))))

(handlers/register-handler-fx
 ::update-network-status
 [re-frame/trim-v]
 (fn [_ [data]]
   {::notify-status-go data}))
