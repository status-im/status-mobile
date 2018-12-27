(ns status-im.signals.core
  (:require [status-im.accounts.db :as accounts.db]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.init.core :as init]
            [status-im.node.core :as node]
            [status-im.pairing.core :as pairing]
            [status-im.contact-recovery.core :as contact-recovery]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.message.core :as transport.message]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.security :as security]
            [status-im.hardwallet.core :as hardwallet]))

(fx/defn status-node-started
  [{db :db :as cofx}]
  (let [{:node/keys [restart? address on-ready]
         :accounts/keys [create]} db]
    (fx/merge cofx
              {:db         (-> db
                               (assoc :node/status :started)
                               (dissoc :node/restart? :node/address))
               :node/ready nil}

              (when restart?
                (node/initialize address))
              (case on-ready
                :login
                (accounts.login/login)
                :verify-account
                (let [{:keys [address password]} (accounts.db/credentials cofx)]
                  (fn [_]
                    {:accounts.login/verify
                     [address password (:realm-error db)]}))
                :create-account
                (fn [_]
                  {:accounts.create/create-account (:password create)})
                :recover-account
                (fn [{:keys [db]}]
                  (let [{:keys [password passphrase]} (:accounts/recover db)]
                    {:accounts.recover/recover-account
                     [(security/mask-data passphrase) password]}))
                :create-keycard-account
                (hardwallet/create-keycard-account)))))

(fx/defn status-node-stopped
  [{db :db}]
  {:db (assoc db :node/status :stopped)})

(fx/defn status-module-initialized [{:keys [db]}]
  {:db                             (assoc db :status-module-initialized? true)
   :init/status-module-initialized nil})

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
      "module.initialized" (status-module-initialized cofx)
      "envelope.sent"      (transport.message/update-envelope-status cofx (:hash event) :sent)
      "envelope.expired"   (transport.message/update-envelope-status cofx (:hash event) :not-sent)
      "bundles.added"      (pairing/handle-bundles-added cofx event)
      "mailserver.request.completed" (mailserver/handle-request-completed cofx event)
      "mailserver.request.expired"   (when (accounts.db/logged-in? cofx)
                                       (mailserver/resend-request cofx {:request-id (:hash event)}))
      "messages.decrypt.failed" (contact-recovery/show-contact-recovery-fx cofx (:sender event))
      "discovery.summary"  (summary cofx event)
      (log/debug "Event " type " not handled"))))
