(ns status-im.signals.core
  (:require [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.i18n.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.login.core :as login]
            [status-im.transport.message.core :as transport.message]
            [status-im.notifications.local :as local-notifications]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.models.link-preview :as link.preview]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn status-node-started
  [{db :db :as cofx} {:keys [error]}]
  (log/debug "[signals] status-node-started"
             "error" error)
  (if error
    (cond-> {:db (-> db
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
      (= (:view-id db) :progress)
      (assoc :dispatch [:navigate-to :login]))
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
  {:events [:signals/signal-received]}
  [cofx event-str]
  ;; We only convert to clojure when strictly necessary or we know it
  ;; won't impact performance, as it is a fairly costly operation on large-ish
  ;; data structures
  (let [^js data (.parse js/JSON event-str)
        ^js event-js (.-event data)
        type (.-type data)]
    (case type
      "node.login"         (status-node-started cofx (js->clj event-js :keywordize-keys true))
      "envelope.sent"      (transport.message/update-envelopes-status cofx (:ids (js->clj event-js :keywordize-keys true)) :sent)
      "envelope.expired"   (transport.message/update-envelopes-status cofx (:ids (js->clj event-js :keywordize-keys true)) :not-sent)
      "message.delivered"  (let [{:keys [chatID messageID]} (js->clj event-js :keywordize-keys true)]
                             (models.message/update-db-message-status cofx chatID messageID :delivered))
      "discovery.summary"  (summary cofx (js->clj event-js :keywordize-keys true))
      "subscriptions.data" (ethereum.subscriptions/handle-signal cofx (js->clj event-js :keywordize-keys true))
      "subscriptions.error" (ethereum.subscriptions/handle-error cofx (js->clj event-js :keywordize-keys true))
      "messages.new" (transport.message/sanitize-messages-and-process-response cofx event-js true)
      "wallet" (ethereum.subscriptions/new-wallet-event cofx (js->clj event-js :keywordize-keys true))
      "local-notifications" (local-notifications/process cofx (js->clj event-js :keywordize-keys true))
      "community.found" (link.preview/cache-community-preview-data (js->clj event-js :keywordize-keys true))
      (log/debug "Event " type " not handled"))))
