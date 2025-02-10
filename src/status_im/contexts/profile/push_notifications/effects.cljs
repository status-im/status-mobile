(ns status-im.contexts.profile.push-notifications.effects
  (:require
    [native-module.push-notifications :as native-module.pn]
    [promesa.core :as promesa]
    [react-native.platform :as platform]
    [status-im.config :as config]
    [status-im.contexts.profile.push-notifications.android-remote :as pn-android-remote]
    [status-im.contexts.push-notifications.notifications :as pn-notifications]
    [status-im.contexts.push-notifications.permissions :as pn-permissions]
    [status-im.contexts.push-notifications.remote-token :as pn-remote-token]
    [utils.re-frame :as rf]))

(defn enable-ios-notifications
  [_settings]
  (-> (pn-permissions/request-notification-permissions)
      (promesa/then
       (fn [result]
         (let [authorized? (get-in result [:ok :authorized?] false)]
           (if authorized?
             (pn-remote-token/request-remote-token {})
             result))))
      (promesa/then
       (fn [result]
         (if-let [error (:error result)]
           (rf/dispatch [:push-notifications/switch-error true error])
           (rf/dispatch [:push-notifications/registered-for-push-notifications
                         (-> result :ok :token)]))))))

(defn disable-ios-notifications
  []
  (pn-permissions/release-notification-permissions)
  (rf/dispatch [:push-notifications/unregistered-from-push-notifications]))

(defn enable-android-notifications
  [remote-push-notifications-enabled?]
  (if (and remote-push-notifications-enabled? (not config/google-free))
    (do
      (native-module.pn/disable-notifications)
      (native-module.pn/clear-all-message-notifications)
      (pn-android-remote/register-remote-notifications))
    (do
      (pn-android-remote/unregister-remote-notifications)
      (native-module.pn/create-channel
       {:channel-id   "status-im-notifications"
        :channel-name "Status push notifications"})
      (native-module.pn/enable-notifications))))

(defn disable-android-notifications
  []
  (native-module.pn/disable-notifications)
  (pn-android-remote/unregister-remote-notifications))

(rf/reg-fx
 :effects/push-notifications-enable
 (fn [remote-push-notifications-enabled?]
   (if platform/android?
     (enable-android-notifications remote-push-notifications-enabled?)
     (enable-ios-notifications))))

(rf/reg-fx
 :effects/push-notifications-disable
 (fn []
   (if platform/android?
     (disable-android-notifications)
     (disable-ios-notifications))))

(rf/reg-fx
 :effects/push-notifications-clear-message-notifications
 (fn [chat-ids]
   (if platform/android?
     (doseq [chat-id chat-ids]
       (native-module.pn/clear-message-notifications chat-id))
     (pn-notifications/clear-received-notifications))))
