(ns status-im.contexts.profile.push-notifications.effects
  (:require
    [native-module.push-notifications :as native-module.pn]
    [promesa.core :as promesa]
    [react-native.platform :as platform]
    [status-im.config :as config]
    [status-im.contexts.push-notifications.notifications :as pn-notifications]
    [status-im.contexts.push-notifications.permissions :as pn-permissions]
    [status-im.contexts.push-notifications.remote-token :as pn-remote-token]
    [utils.re-frame :as rf]))

(defn enable-remote-notifications
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

(defn disable-remote-notifications
  []
  (pn-permissions/release-notification-permissions)
  (rf/dispatch [:push-notifications/unregistered-from-push-notifications]))

(defn enable-ios-notifications
  [_settings]
  (enable-remote-notifications {}))

(defn disable-ios-notifications
  []
  (disable-remote-notifications))

(defn enable-android-notifications
  [{:keys [enable-remote? enable-local?]}]
  (when (and enable-remote? (not config/google-free))
    (enable-remote-notifications {}))
  (when enable-local?
    (native-module.pn/create-channel
     {:channel-id   "status-im-notifications"
      :channel-name "Status push notifications"})
    (native-module.pn/enable-notifications)))

(defn enable-push-notifications
  [settings]
  (if platform/android?
    (enable-android-notifications settings)
    (enable-ios-notifications settings)))

(defn disable-android-notifications
  [{:keys [disable-local? disable-remote?]}]
  (when (and disable-remote? (not config/google-free))
    (disable-remote-notifications))
  (when disable-local?
    (native-module.pn/disable-notifications))
  ;; (native-module.pn/clear-all-message-notifications)
)

(rf/reg-fx
 :effects/push-notifications-enable
 (fn [settings]
   (enable-push-notifications settings)))

(rf/reg-fx
 :effects/push-notifications-disable
 (fn [settings]
   (if platform/android?
     (disable-android-notifications settings)
     (disable-ios-notifications))))

(rf/reg-fx
 :effects/push-notifications-clear-message-notifications
 (fn [chat-ids]
   (if platform/android?
     (doseq [chat-id chat-ids]
       (native-module.pn/clear-message-notifications chat-id))
     (pn-notifications/clear-received-notifications))))
