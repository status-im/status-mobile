(ns status-im.signals.core
  (:require [status-im.chat.models.loading :as chat.loading]
            [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.node.core :as node]
            [status-im.pairing.core :as pairing]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.transport.message.core :as transport.message]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.multiaccounts.login.core :as login]))

(fx/defn status-node-started
  [{db :db :as cofx} event]
  (login/multiaccount-login-success cofx))

(fx/defn summary
  [{:keys [db] :as cofx} peers-summary]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (fx/merge cofx
              {:db (assoc db
                          :peers-summary peers-summary
                          :peers-count peers-count)}
              (mailserver/peers-summary-change previous-summary))))

(fx/defn process
  [cofx event-str]
  (let [{:keys [type event]} (types/json->clj event-str)]
    (case type
      "node.login"         (status-node-started cofx event)
      "envelope.sent"      (transport.message/update-envelopes-status cofx (:ids event) :sent)
      "envelope.expired"   (transport.message/update-envelopes-status cofx (:ids event) :not-sent)
      "bundles.added"      (pairing/handle-bundles-added cofx event)
      "mailserver.request.completed" (mailserver/handle-request-completed cofx event)
      "mailserver.request.expired"   (when (multiaccounts.model/logged-in? cofx)
                                       (mailserver/resend-request cofx {:request-id (:hash event)}))
      "discovery.summary"  (summary cofx event)
      "subscriptions.data" (ethereum.subscriptions/handle-signal cofx event)
      "subscriptions.error" (ethereum.subscriptions/handle-error cofx event)
      "whisper.filter.added" (transport.filters/handle-negotiated-filter cofx event)
      "messages.new" (transport.message/receive-messages cofx event)
      "wallet" (ethereum.subscriptions/new-wallet-event cofx event)
      (log/debug "Event " type " not handled" event))))
