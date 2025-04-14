(ns status-im.contexts.profile.push-notifications.events
  (:require
    [cljs-bean.core :as bean]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [native-module.push-notifications :as native-module.pn]
    [re-frame.core :as re-frame]
    [react-native.async-storage :as async-storage]
    [react-native.platform :as platform]
    [react-native.push-notification-ios :as pn-ios]
    [status-im.common.json-rpc.events :as json-rpc]
    [status-im.config :as config]
    status-im.contexts.profile.push-notifications.effects
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(def apn-token-type 1)
(def firebase-token-type 2)

(defn handle-notification-press
  [{{deep-link :deepLink} :userInfo
    interaction           :userInteraction}]
  (async-storage/set-item! (str :chat-id) nil #(rf/dispatch [:universal-links/remove-handling]))
  (when (and deep-link (or platform/ios? (and platform/android? interaction)))
    (rf/dispatch [:universal-links/handling])
    (rf/dispatch [:universal-links/handle-url deep-link])))

(defn listen-notifications
  []
  (if platform/ios?
    (pn-ios/add-listener "localNotification"
                         #(handle-notification-press {:userInfo (bean/bean (.getData ^js %))}))
    (native-module.pn/add-listener "remoteNotificationReceived"
                                   #(handle-notification-press (transforms/json->clj %)))))

(rf/defn handle-enable-notifications-event
  {:events [:push-notifications/registered-for-push-notifications]}
  [_ token]
  {:json-rpc/call [{:method     "wakuext_registerForPushNotifications"
                    :params     [token (when platform/ios? config/apn-topic)
                                 (if platform/ios? apn-token-type firebase-token-type)]
                    :on-success #(log/info "[push-notifications] register-success" %)
                    :on-error   #(log/info "[push-notifications] register-error" %)}]})

(rf/defn handle-disable-notifications-event
  {:events [:push-notifications/unregistered-from-push-notifications]}
  [_]
  {:json-rpc/call [{:method     "wakuext_unregisterFromPushNotifications"
                    :params     []
                    :on-success #(log/info "[push-notifications] unregister-success" %)
                    :on-error   #(log/info "[push-notifications] unregister-error" %)}]})

(rf/defn handle-preferences-load
  {:events [:push-notifications/preferences-loaded]}
  [{:keys [db]} preferences]
  {:db (assoc db :push-notifications/preferences preferences)})

(re-frame/reg-fx :push-notifications/load-preferences
 (fn []
   (json-rpc/call {:method     "localnotifications_notificationPreferences"
                   :params     []
                   :on-success [:push-notifications/preferences-loaded]})))

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

(defn- change-profile-setting-db
  [db {:keys [setting value]}]
  (assoc-in db [:profile/profile setting] value))

(defn- save-profile-setting-fx
  [{:keys [setting value on-success]}]
  [:json-rpc/call
   [{:method     "settings_saveSetting"
     :params     [setting value]
     :on-success (or on-success #())}]])

(defn notifications-switch
  [{:keys [db]} [enabled?]]
  (let [{:keys [notifications-enabled?
                news-notifications-enabled?
                messenger-notifications-enabled?]} (:profile/profile db)
        enable-notifications?                      (and (not notifications-enabled?) enabled?)
        disable-notifications?                     (and notifications-enabled? (not enabled?))
        should-enable-messenger-notifications?     (and enabled? (nil? messenger-notifications-enabled?))
        permission-setting                         {:setting :notifications-enabled?
                                                    :value   enabled?}
        messenger-setting                          {:setting :messenger-notifications-enabled?
                                                    :value   (if (some? messenger-notifications-enabled?)
                                                               messenger-notifications-enabled?
                                                               true)}]
    {:db (cond-> db
           should-enable-messenger-notifications?
           (change-profile-setting-db messenger-setting)
           :always
           (change-profile-setting-db permission-setting))
     :fx [(cond
            enable-notifications?
            [:effects/push-notifications-enable
             (cond-> #{}
               (not (false? messenger-notifications-enabled?)) (conj :enable-chat-notifications?)
               news-notifications-enabled?                     (conj :enable-news-notifications?))]
            disable-notifications?
            [:effects/push-notifications-disable
             (cond-> #{}
               news-notifications-enabled?      (conj :disable-news-notifications?)
               messenger-notifications-enabled? (conj :disable-chat-notifications?))]
            :else nil)
          (when enabled?
            (save-profile-setting-fx permission-setting))
          (when should-enable-messenger-notifications?
            (save-profile-setting-fx messenger-setting))]}))

(rf/reg-event-fx :push-notifications/switch notifications-switch)

(defn messenger-notifications-switch
  [{:keys [db]} [enabled?]]
  (let [prev-enabled?               (get-in db [:profile/profile :messenger-notifications-enabled?])
        enable-chat-notifications?  (and (not prev-enabled?) enabled?)
        disable-chat-notifications? (and prev-enabled? (not enabled?))
        setting                     {:setting :messenger-notifications-enabled?
                                     :value   enabled?}]
    {:db (change-profile-setting-db db setting)
     :fx [(cond
            enable-chat-notifications?
            [:effects/push-notifications-enable
             #{:enable-chat-notifications?}]
            disable-chat-notifications?
            [:effects/push-notifications-disable
             #{:disable-chat-notifications?}]
            :else nil)
          (when (some? enabled?)
            (save-profile-setting-fx setting))]}))

(rf/reg-event-fx :notifications/messenger-notifications-switch messenger-notifications-switch)

(defn news-notifications-switch
  [{:keys [db]} [enabled?]]
  (let [prev-enabled?               (get-in db [:profile/profile :news-notifications-enabled?])
        enable-news-notifications?  (and (not prev-enabled?) enabled?)
        disable-news-notifications? (and prev-enabled? (not enabled?))
        setting                     {:setting :news-notifications-enabled?
                                     :value   enabled?}]
    {:db (change-profile-setting-db db setting)
     :fx [(cond
            enable-news-notifications?
            [:effects/push-notifications-enable
             #{:enable-news-notifications?}]
            disable-news-notifications?
            [:effects/push-notifications-disable
             #{:disable-news-notifications?}]
            :else nil)
          (when (some? enabled?)
            (save-profile-setting-fx setting))]}))

(rf/reg-event-fx :notifications/news-notifications-switch news-notifications-switch)
