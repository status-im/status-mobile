(ns status-im.common.signals.events
  (:require
    [legacy.status-im.chat.models.message :as models.message]
    [legacy.status-im.ethereum.subscriptions :as ethereum.subscriptions]
    [legacy.status-im.mailserver.core :as mailserver]
    [legacy.status-im.visibility-status-updates.core :as visibility-status-updates]
    [status-im.common.pairing.events :as pairing]
    [status-im.contexts.chat.messages.link-preview.events :as link-preview]
    [status-im.contexts.chat.messages.transport.events :as messages.transport]
    [status-im.contexts.communities.discover.events]
    [status-im.contexts.profile.login.events :as profile.login]
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
                          :peers-summary peers-summary
                          :peers-count   peers-count)}
              (visibility-status-updates/peers-summary-change peers-count))))

(rf/defn wakuv2-peer-stats
  [{:keys [db]} peer-stats]
  {:db (assoc db :peer-stats peer-stats :peers-count (count (:peers peer-stats)))})

(rf/defn process
  {:events [:signals/signal-received]}
  [{:keys [db] :as cofx} event-str]
  ;; We only convert to clojure when strictly necessary or we know it
  ;; won't impact performance, as it is a fairly costly operation on large-ish
  ;; data structures
  (let [^js data     (.parse js/JSON event-str)
        ^js event-js (.-event data)
        type         (.-type data)]
    (case type
      "node.login"                 (profile.login/login-node-signal cofx (transforms/js->clj event-js))
      "backup.performed"           {:db (assoc-in db
                                         [:profile/profile :last-backup]
                                         (.-lastBackup event-js))}
      "envelope.sent"              (messages.transport/update-envelopes-status cofx
                                                                               (:ids
                                                                                (js->clj event-js
                                                                                         :keywordize-keys
                                                                                         true))
                                                                               :sent)
      "envelope.expired"           (messages.transport/update-envelopes-status cofx
                                                                               (:ids
                                                                                (js->clj event-js
                                                                                         :keywordize-keys
                                                                                         true))
                                                                               :not-sent)
      "message.delivered"          (let [{:keys [chatID messageID]} (js->clj event-js
                                                                             :keywordize-keys
                                                                             true)]
                                     (models.message/update-message-status cofx
                                                                           chatID
                                                                           messageID
                                                                           :delivered))
      "mailserver.changed"         (mailserver/handle-mailserver-changed cofx (.-id event-js))
      "mailserver.available"       (mailserver/handle-mailserver-available cofx (.-id event-js))
      "mailserver.not.working"     (mailserver/handle-mailserver-not-working cofx)
      "discovery.summary"          (summary cofx (js->clj event-js :keywordize-keys true))
      "mediaserver.started"        {:db (assoc db :mediaserver/port (.-port event-js))}
      "wakuv2.peerstats"           (wakuv2-peer-stats cofx (js->clj event-js :keywordize-keys true))
      "messages.new"               (messages.transport/sanitize-messages-and-process-response cofx
                                                                                              event-js
                                                                                              true)
      "wallet"                     (ethereum.subscriptions/new-wallet-event cofx
                                                                            (js->clj event-js
                                                                                     :keywordize-keys
                                                                                     true))
      "local-notifications"        (local-notifications/process cofx
                                                                (js->clj event-js :keywordize-keys true))
      "community.found"            (link-preview/cache-community-preview-data (js->clj event-js
                                                                                       :keywordize-keys
                                                                                       true))
      "status.updates.timedout"    (visibility-status-updates/handle-visibility-status-updates
                                    cofx
                                    (js->clj event-js :keywordize-keys true))
      "localPairing"               (pairing/handle-local-pairing-signals
                                    cofx
                                    (js->clj event-js :keywordize-keys true))
      "curated.communities.update" (rf/dispatch [:fetched-contract-communities
                                                 (js->clj event-js :keywordize-keys true)])
      "waku.backedup.profile"      (rf/dispatch [:profile/update-profile-from-backup
                                                 (js->clj event-js :keywordize-keys true)])
      "waku.backedup.settings"     (rf/dispatch [:profile/update-setting-from-backup
                                                 (js->clj event-js :keywordize-keys true)])

      (log/debug "Event " type " not handled"))))
