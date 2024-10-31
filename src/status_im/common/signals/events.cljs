(ns status-im.common.signals.events
  (:require
    [legacy.status-im.chat.models.message :as models.message]
    [legacy.status-im.visibility-status-updates.core :as visibility-status-updates]
    [oops.core :as oops]
    [status-im.common.pairing.events :as pairing]
    [status-im.contexts.chat.messenger.messages.transport.events :as messages.transport]
    [status-im.contexts.communities.discover.events]
    [status-im.contexts.profile.push-notifications.local.events :as local-notifications]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/defn summary
  [{:keys [db] :as cofx} peers-summary]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (rf/merge cofx
              {:db (assoc db
                          :peers-summary    peers-summary
                          :peer-stats/count peers-count)}
              (visibility-status-updates/peers-summary-change peers-count))))

(rf/defn process
  {:events [:signals/signal-received]}
  [{:keys [db] :as cofx} event-str]
  ;; We only convert to clojure when strictly necessary or we know it
  ;; won't impact performance, as it is a fairly costly operation on large-ish
  ;; data structures
  (let [^js data     (.parse js/JSON event-str)
        ^js event-js (.-event data)
        type         (.-type data)]
    (log/debug "Signal received" {:type type})
    (log/trace "Signal received" {:payload event-str})
    (case type
      "wallet"
      {:fx [[:dispatch [:wallet/signal-received event-js]]]}

      "wallet.suggested.routes"
      {:fx [[:dispatch [:wallet/handle-suggested-routes (transforms/js->clj event-js)]]]}

      "wallet.router.sign-transactions"
      {:fx [[:dispatch [:wallet/sign-transactions-signal-received (transforms/js->clj event-js)]]]}

      "wallet.router.transactions-sent"
      {:fx [[:dispatch [:wallet/transactions-sent-signal-received (transforms/js->clj event-js)]]]}

      "envelope.sent"
      (messages.transport/update-envelopes-status
       cofx
       (:ids (transforms/js->clj event-js))
       :sent)

      "envelope.expired"
      (messages.transport/update-envelopes-status
       cofx
       (:ids (transforms/js->clj event-js))
       :not-sent)

      "message.delivered"
      (let [{:keys [chatID messageID]} (transforms/js->clj event-js)]
        (models.message/update-message-status cofx chatID messageID :delivered))

      "messages.new"
      (messages.transport/sanitize-messages-and-process-response cofx event-js true)

      "discovery.summary"
      (summary cofx (transforms/js->clj event-js))

      "local-notifications"
      (local-notifications/process cofx (transforms/js->clj event-js))

      "community.found"
      (let [community (transforms/js->clj event-js)]
        {:fx [[:dispatch [:discover-community/maybe-found-unknown-contract-community community]]]})

      "status.updates.timedout"
      (visibility-status-updates/handle-visibility-status-updates cofx (transforms/js->clj event-js))

      "localPairing"
      (pairing/handle-local-pairing-signals cofx (transforms/js->clj event-js))

      "curated.communities.update"
      {:fx [[:dispatch [:fetched-contract-communities (transforms/js->clj event-js)]]]}

      "waku.backedup.profile"
      {:fx [[:dispatch [:profile/update-profile-from-backup (transforms/js->clj event-js)]]]}

      "waku.backedup.settings"
      {:fx [[:dispatch [:profile/update-setting-from-backup (transforms/js->clj event-js)]]]}

      "waku.backedup.keypair"
      {:fx [[:dispatch [:wallet/process-keypair-from-backup (transforms/js->clj event-js)]]]}

      "waku.backedup.watch-only-account"
      {:fx [[:dispatch [:wallet/process-watch-only-account-from-backup (transforms/js->clj event-js)]]]}

      "mediaserver.started"
      {:db (assoc db :mediaserver/port (oops/oget event-js :port))}

      "node.login"
      {:fx [[:dispatch [:profile.login/login-node-signal (transforms/js->clj event-js)]]]}

      "backup.performed"
      {:db (assoc-in db [:profile/profile :last-backup] (oops/oget event-js :lastBackup))}

      (log/debug "Event " type " not handled"))))
