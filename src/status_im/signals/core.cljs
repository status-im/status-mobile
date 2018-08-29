(ns status-im.signals.core
  (:require [status-im.accounts.db :as accounts.db]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.init.core :as init]
            [status-im.node.core :as node]
            [status-im.transport.handlers :as transport.handlers]
            [status-im.transport.inbox :as inbox]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

(fx/defn status-node-started
  [{db :db :as cofx}]
  (fx/merge cofx
            {:db (assoc db :status-node-started? true)}
            #(when (:password (accounts.db/credentials cofx))
               (accounts.login/login %))))

(fx/defn status-node-stopped
  [cofx]
  (let [{:keys [address]} (accounts.db/credentials cofx)]
    (node/start cofx address)))

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
              (transport.handlers/resend-contact-messages previous-summary)
              (inbox/peers-summary-change previous-summary))))

(fx/defn process
  [cofx event-str]
  (let [{:keys [type event]} (types/json->clj event-str)]
    (case type
      "node.started"       (status-node-started cofx)
      "node.stopped"       (status-node-stopped cofx)
      "module.initialized" (status-module-initialized cofx)
      "envelope.sent"      (transport.handlers/update-envelope-status cofx (:hash event) :sent)
      "envelope.expired"   (transport.handlers/update-envelope-status cofx (:hash event) :sent)
      "messages.decrypt.failed" {:dispatch [:signals/message-decrypt-failed (:sender event)]}
      "discovery.summary"  (summary cofx event)
      (log/debug "Event " type " not handled"))))
