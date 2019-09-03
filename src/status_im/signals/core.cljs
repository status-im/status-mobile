(ns status-im.signals.core
  (:require [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.login.core :as login]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.pairing.core :as pairing]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.transport.message.core :as transport.message]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(fx/defn status-node-started
  [{db :db :as cofx} {:keys [error]}]
  (if error
    {:db (-> db
             (update :multiaccounts/login dissoc :processing)
             (assoc-in [:multiaccounts/login :error]
                       ;; NOTE: the only currently known error is
                       ;; "file is not a database" which occurs
                       ;; when the user inputs the wrong password
                       ;; if that is the error that is found
                       ;; we show the i18n label for wrong password
                       ;; to the user
                       ;; in case of an unknown error we show the
                       ;; error
                       (if (= error "file is not a database")
                         (i18n/label :t/wrong-password)
                         error)))}
    (login/multiaccount-login-success cofx)))

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
