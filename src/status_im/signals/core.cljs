(ns status-im.signals.core
  (:require [status-im.init.core :as init]
            [status-im.transport.handlers :as transport.handlers]
            [status-im.transport.inbox :as inbox]
            [status-im.ui.screens.accounts.login.models :as login]
            [status-im.node.models :as node]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn status-node-started
  [{db :db :as cofx}]
  (let [fx {:db (assoc db :status-node-started? true)}]
    (if (:password (login/credentials cofx))
      (handlers-macro/merge-fx cofx
                               fx
                               (login/login))
      fx)))

(defn status-node-stopped
  [cofx]
  (let [{:keys [address]} (login/credentials cofx)]
    (node/start address cofx)))

(defn status-module-initialized [{:keys [db]}]
  {:db                             (assoc db :status-module-initialized? true)
   :init/status-module-initialized nil})

(defn summary [peers-summary {:keys [db] :as cofx}]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db
                                         :peers-summary peers-summary
                                         :peers-count peers-count)}
                             (transport.handlers/resend-contact-messages previous-summary)
                             (inbox/peers-summary-change-fx previous-summary))))

(defn process [event-str cofx]
  (let [{:keys [type event]} (types/json->clj event-str)]
    (case type
      "node.started"       (status-node-started cofx)
      "node.stopped"       (status-node-stopped cofx)
      "module.initialized" (status-module-initialized cofx)
      "envelope.sent"      (transport.handlers/update-envelope-status (:hash event) :sent cofx)
      "envelope.expired"   (transport.handlers/update-envelope-status (:hash event) :sent cofx)
      "discovery.summary"  (summary event cofx)
      (log/debug "Event " type " not handled"))))
