(ns status-im.network.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.network.net-info :as net-info]
            [status-im.native-module.core :as status]
            [status-im.transport.inbox :as inbox]))

(re-frame/reg-fx
 :network/listen-to-network-status
 (fn [[connection-listener net-info-listener]]
   (net-info/is-connected? connection-listener)
   (net-info/net-info net-info-listener)
   (net-info/add-connection-listener connection-listener)
   (net-info/add-net-info-listener net-info-listener)))

(re-frame/reg-fx
 ::notify-status-go
 (fn [data]
   (status/connection-change data)))

(handlers/register-handler-fx
 :network/update-connection-status
 (fn [{db :db :as cofx} [_ is-connected?]]
   (handlers-macro/merge-fx
    cofx
    {:db (assoc db :network-status (if is-connected? :online :offline))}
    (inbox/request-messages))))

(handlers/register-handler-fx
 :network/update-network-status
 (fn [_ [_ data]]
   {::notify-status-go data}))
