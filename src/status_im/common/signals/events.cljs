(ns status-im.common.signals.events
  (:require
    [legacy.status-im.chat.models.message :as models.message]
    [legacy.status-im.mailserver.core :as mailserver]
    [legacy.status-im.visibility-status-updates.core :as visibility-status-updates]
    [oops.core :as oops]
    [status-im.common.pairing.events :as pairing]
    [status-im.contexts.chat.messenger.messages.link-preview.events :as link-preview]
    [status-im.contexts.chat.messenger.messages.transport.events :as messages.transport]
    [status-im.contexts.communities.discover.events]
    [status-im.contexts.profile.push-notifications.local.events :as local-notifications]
    [taoensso.timbre :as log]
    [utils.debounce :as debounce]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/defn summary
  [{:keys [db] :as cofx} peers-summary]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (rf/merge cofx
              {:db (assoc db
                          :peers-summary peers-summary
                          :peers-count   peers-count)}
              (visibility-status-updates/peers-summary-change peers-count))))

(rf/reg-event-fx :wakuv2-peer-stats
 (fn [{:keys [db]} [^js peer-stats-js]]
   (let [peer-stats (transforms/js->clj peer-stats-js)]
     {:db (assoc db
                 :peer-stats  peer-stats
                 :peers-count (count (:peers peer-stats)))})))

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

      "wakuv2.peerstats"
      (debounce/debounce-and-dispatch [:wakuv2-peer-stats event-js] 1000)

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

      "mailserver.changed"
      (mailserver/handle-mailserver-changed cofx (oops/oget event-js :id))

      "mailserver.available"
      (mailserver/handle-mailserver-available cofx (oops/oget event-js :id))

      "mailserver.not.working"
      (mailserver/handle-mailserver-not-working cofx)

      "discovery.summary"
      (summary cofx (transforms/js->clj event-js))

      "local-notifications"
      (local-notifications/process cofx (transforms/js->clj event-js))

      "community.found"
      (let [community (transforms/js->clj event-js)]
        {:fx [[:dispatch
               [:chat.ui/cache-link-preview-data (link-preview/community-link (:id community))
                community]]
              [:dispatch [:discover-community/maybe-found-unknown-contract-community community]]]})

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
