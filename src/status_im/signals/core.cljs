(ns status-im.signals.core
  (:require [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.contact-recovery.core :as contact-recovery]
            [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.mailserver.core :as mailserver]
            [status-im.node.core :as node]
            [status-im.pairing.core :as pairing]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.utils.fx :as fx]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(fx/defn status-node-started
  [{db :db :as cofx}]
  (let [{:node/keys [restart? address on-ready]
         :multiaccounts/keys [create]} db]
    (fx/merge cofx
              {:db         (-> db
                               (assoc :node/status :started)
                               (dissoc :node/restart? :node/address))
               :node/ready nil}

              (when restart?
                (node/initialize address))
              (case on-ready
                :login
                (multiaccounts.login/login)
                :verify-multiaccount
                (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
                  (fn [_]
                    {:multiaccounts.login/verify
                     [address password (:realm-error db)]}))
                :create-multiaccount
                (fn [_]
                  {:multiaccounts.create/create-multiaccount (select-keys create [:id :password])})
                :recover-multiaccount
                (fn [{:keys [db]}]
                  (let [{:keys [password passphrase]} (:multiaccounts/recover db)]
                    {:multiaccounts.recover/recover-multiaccount
                     [(security/mask-data passphrase) password]}))
                :create-keycard-multiaccount
                (hardwallet/create-keycard-multiaccount)))))

(fx/defn status-node-stopped
  [{db :db}]
  {:db (assoc db :node/status :stopped)})

(fx/defn summary
  [{:keys [db] :as cofx} peers-summary]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (fx/merge cofx
              {:db (assoc db
                          :peers-summary peers-summary
                          :peers-count peers-count)}
              (transport.message/resend-contact-messages previous-summary)
              (mailserver/peers-summary-change previous-summary))))

(fx/defn process
  [cofx event-str]
  (let [{:keys [type event]} (types/json->clj event-str)]
    (case type
      "node.ready"         (status-node-started cofx)
      "node.stopped"       (status-node-stopped cofx)
      "envelope.sent"      (transport.message/update-envelope-status cofx (:hash event) :sent)
      "envelope.expired"   (transport.message/update-envelope-status cofx (:hash event) :not-sent)
      "bundles.added"      (pairing/handle-bundles-added cofx event)
      "mailserver.request.completed" (mailserver/handle-request-completed cofx event)
      "mailserver.request.expired"   (when (multiaccounts.model/logged-in? cofx)
                                       (mailserver/resend-request cofx {:request-id (:hash event)}))
      "messages.decrypt.failed" (contact-recovery/handle-contact-recovery-fx cofx (:sender event))
      "discovery.summary"  (summary cofx event)
      "subscriptions.data" (ethereum.subscriptions/handle-signal cofx event)
      "subscriptions.error" (ethereum.subscriptions/handle-error cofx event)
      "status.chats.did-change" (chat.loading/load-chats-from-rpc cofx)
      "whisper.filter.added" (transport.filters/handle-negotiated-filter cofx event)
      "messages.new" (transport.message/receive-messages cofx event)
      "wallet" (ethereum.subscriptions/new-wallet-event cofx event)
      (log/debug "Event " type " not handled" event))))
