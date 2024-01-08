(ns status-im.contexts.profile.push-notifications.events
  (:require
    [cljs-bean.core :as bean]
    [native-module.push-notifications :as native-module.pn]
    [react-native.async-storage :as async-storage]
    [react-native.platform :as platform]
    [react-native.push-notification-ios :as pn-ios]
    [status-im.config :as config]
    status-im.contexts.profile.push-notifications.effects
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(def server-type-default 1)
(def server-type-custom 2)

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
                    :params     [token (if platform/ios? config/apn-topic)
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

(rf/defn load-preferences
  [_]
  {:json-rpc/call [{:method     "localnotifications_notificationPreferences"
                    :params     []
                    :on-success #(rf/dispatch [:push-notifications/preferences-loaded %])}]})
