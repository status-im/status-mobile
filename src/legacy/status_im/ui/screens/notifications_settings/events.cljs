(ns legacy.status-im.ui.screens.notifications-settings.events
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [react-native.platform :as platform]
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
  [cofx enabled?]
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
  [cofx enabled?]
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
  [{:keys [db] :as cofx} options]
  (let [profile (:profile/profile db)
        prev-settings {:notifications-enabled?             (:notifications-enabled? profile)
                       :local-push-notifications-enabled?  (:local-push-notifications-enabled? profile)
                       :remote-push-notifications-enabled? (:remote-push-notifications-enabled? profile)}
        next-settings
        (cond
          (and (:notifications-enabled? options)
               (not (:notifications-enabled? prev-settings)))
          (if platform/android?
            {:notifications-enabled?             true
             :local-push-notifications-enabled?  false
             :remote-push-notifications-enabled? false}
            {:notifications-enabled?             true
             :local-push-notifications-enabled?  true
             :remote-push-notifications-enabled? true})

          (and (not (:notifications-enabled? options))
               (not (nil? (:notifications-enabled? options)))
               (:notifications-enabled? prev-settings))
          {:notifications-enabled?             false
           :local-push-notifications-enabled?  false
           :remote-push-notifications-enabled? false}

          (and (:notifications-enabled? prev-settings)
               (not (:local-push-notifications-enabled? options))
               (not (:local-push-notifications-enabled? prev-settings))
               (not (:remote-push-notifications-enabled? options))
               (not (:remote-push-notifications-enabled? prev-settings)))
          {:notifications-enabled?             false
           :local-push-notifications-enabled?  false
           :remote-push-notifications-enabled? false}

          :else
          {:notifications-enabled? (:notifications-enabled? prev-settings)
           :local-push-notifications-enabled?
           (if (nil? (:local-push-notifications-enabled? options))
             (:local-push-notifications-enabled? prev-settings)
             (:local-push-notifications-enabled? options))
           :remote-push-notifications-enabled?
           (if (nil? (:remote-push-notifications-enabled? options))
             (:remote-push-notifications-enabled? prev-settings)
             (:remote-push-notifications-enabled? options))})
        any-disabled?
        (or
         (and (:local-push-notifications-enabled? prev-settings)
              (not (:local-push-notifications-enabled? next-settings)))
         (and (:remote-push-notifications-enabled? prev-settings)
              (not (:remote-push-notifications-enabled? next-settings))))]
    (rf/merge
     cofx
     (if (and (:notifications-enabled? next-settings)
              (not any-disabled?))
       {:effects/push-notifications-enable {:prev-settings prev-settings
                                            :settings      next-settings}}
       {:effects/push-notifications-disable {:prev-settings prev-settings
                                             :settings      next-settings}})
     (multiaccounts.update/multiaccount-update :notifications-enabled?
                                               (:notifications-enabled? next-settings)
                                               {})
     (multiaccounts.update/multiaccount-update :local-push-notifications-enabled?
                                               (:local-push-notifications-enabled? next-settings)
                                               {})
     (multiaccounts.update/multiaccount-update :remote-push-notifications-enabled?
                                               (:remote-push-notifications-enabled? next-settings)
                                               {}))))
