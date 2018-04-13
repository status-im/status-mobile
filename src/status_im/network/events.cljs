(ns status-im.network.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.network.net-info :as net-info]
            [status-im.native-module.core :as status]
            [status-im.transport.inbox :as inbox]))

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

(handlers/register-handler-fx
  :listen-to-network-status
  (fn []
    {::listen-to-network-status [#(re-frame/dispatch [::update-connection-status %])
                                 #(re-frame/dispatch [::update-network-status %])]}))

(handlers/register-handler-fx
  ::update-connection-status
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [is-connected?]]
    (let [previous-status (:network-status db)
          back-online?    (and (= previous-status :offline)
                               is-connected?)]
      (cond-> (handlers-macro/merge-fx cofx
                                       {:db (assoc db :network-status (if is-connected? :online :offline))}
                                       (inbox/recover-offline-inbox back-online?))
        is-connected?
        (assoc :drain-mixpanel-events nil)))))

(handlers/register-handler-fx
  ::update-network-status
  [re-frame/trim-v]
  (fn [_ [data]]
    {::notify-status-go data}))
