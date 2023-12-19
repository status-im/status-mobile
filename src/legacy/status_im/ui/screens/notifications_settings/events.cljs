(ns legacy.status-im.ui.screens.notifications-settings.events
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn notification-non-contacts-error
  {:events [:push-notifications/non-contacts-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx
                                   :push-notifications-from-contacts-only?
                                   (not (boolean enabled?))))

(rf/defn notification-block-mentions-error
  {:events [:push-notifications/block-mentions-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx :push-notifications-block-mentions? (not (boolean enabled?))))

(rf/defn notification-non-contacts
  {:events [:push-notifications/switch-non-contacts]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_enablePushNotificationsFromContactsOnly"
                 "wakuext_disablePushNotificationsFromContactsOnly")]
    (rf/merge
     cofx
     {:json-rpc/call [{:method     method
                       :params     []
                       :on-success #(log/info "[push-notifications] contacts-notification-success" %)
                       :on-error   #(log/info "[push-notifications] contacts-notification-error" %)}]}
     (multiaccounts.update/optimistic :push-notifications-from-contacts-only? (boolean enabled?)))))

(rf/defn notification-block-mentions
  {:events [:push-notifications/switch-block-mentions]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_enablePushNotificationsBlockMentions"
                 "wakuext_disablePushNotificationsBlockMentions")]
    (rf/merge cofx
              {:json-rpc/call [{:method     method
                                :params     []
                                :on-success #(log/info "[push-notifications] block-mentions-success" %)
                                :on-error   #(rf/dispatch
                                              [:push-notifications/block-mentions-update-error enabled?
                                               %])}]}

              (multiaccounts.update/optimistic :push-notifications-block-mentions? (boolean enabled?)))))

(rf/defn notification-switch
  {:events [:push-notifications/switch]}
  [{:keys [db] :as cofx} enabled?]
  (rf/merge cofx
            (if enabled?
              {:effects/push-notifications-enable nil}
              {:effects/push-notifications-disable nil})
            (multiaccounts.update/multiaccount-update :notifications-enabled? enabled? {})))
