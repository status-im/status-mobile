(ns status-im.contexts.profile.push-notifications.effects
  (:require
    [native-module.push-notifications :as native-module.pn]
    [promesa.core :as promesa]
    [react-native.platform :as platform]
    [status-im.common.log :as logger]
    [status-im.config :as config]
    [status-im.contexts.push-notifications.notifications :as pn-notifications]
    [status-im.contexts.push-notifications.permissions :as pn-permissions]
    [status-im.contexts.push-notifications.remote-token :as pn-remote-token]
    [utils.re-frame :as rf]))

(defn- attempt-enable-notifications
  []
  (-> (pn-permissions/request-notification-permissions)
      (promesa/then
       (fn [permissions]
         (if (:authorized? permissions)
           permissions
           (promesa/rejected
            (ex-info
             "Failed to authorize push notifications"
             {:description "Requested and failed to authorize permissions for push notifications"})))))))

(defn- enable-news-notifications
  []
  (-> (attempt-enable-notifications)
      (promesa/then
       (fn [_permissions]
         (pn-remote-token/request-fcm-token {})))
      (promesa/then
       (fn [{:keys [_fcm-token]}]
         (logger/log-debug "registered fcm token for status news"
                           ;; NOTE(@seanstrom): temporarily avoid logging fcm-token
                           #_{:fcm-token fcm-token})))
      (promesa/catch
        (fn [error]
          (logger/log-debug "failed to register fcm token for status news"
                            {:error error})))))

(defn- disable-news-notifications
  []
  (pn-remote-token/revoke-fcm-token {}))

(defn- enable-chat-notifications-ios
  []
  (-> (attempt-enable-notifications)
      (promesa/then
       (fn [_permissions]
         (pn-remote-token/request-apns-token {})))
      (promesa/then
       (fn [{:keys [apns-token]}]
         ;; NOTE(seanstrom): We're only registering iOS devices for remote chat push notifications
         ;; because Android will use local notifications from the background for chat
         ;; notifications. This is a temporary workaround that attempts to prevent Android users
         ;; from receiving two push notifications for each chat message when the app is running
         ;; the background. This workaround can be removed after we support remote push
         ;; notifications with message previews that decrypt locally.
         (when platform/ios?
           (rf/dispatch [:push-notifications/registered-for-push-notifications apns-token]))))))

(defn- disable-chat-notifications-ios
  []
  (pn-permissions/release-notification-permissions)
  (rf/dispatch [:push-notifications/unregistered-from-push-notifications]))

(defn- enable-ios-notifications
  [settings]
  (when (contains? settings :enable-chat-notifications?)
    (enable-chat-notifications-ios))
  (when (contains? settings :enable-news-notifications?)
    (enable-news-notifications)))

(defn- disable-ios-notifications
  [settings]
  (when (contains? settings :disable-chat-notifications?)
    (disable-chat-notifications-ios))
  (when (contains? settings :disable-news-notifications?)
    (disable-news-notifications)))

(defn- enable-chat-notifications-android
  []
  (-> (attempt-enable-notifications)
      (promesa/then
       (fn [_permissions]
         (native-module.pn/create-channel
          {:channel-id   "status-im-notifications"
           :channel-name "Status push notifications"})
         (native-module.pn/enable-notifications)))))

(defn- enable-android-notifications
  [settings]
  (when (and (not config/fdroid?)
             (contains? settings :enable-news-notifications?))
    (enable-news-notifications))
  (when (contains? settings :enable-chat-notifications?)
    (enable-chat-notifications-android)))

(defn- enable-push-notifications
  [settings]
  (if platform/android?
    (enable-android-notifications settings)
    (enable-ios-notifications settings)))

(defn- disable-android-notifications
  [settings]
  (when (contains? settings :disable-chat-notifications?)
    (native-module.pn/disable-notifications)
    (native-module.pn/clear-all-message-notifications))
  (when (contains? settings :disable-news-notifications?)
    (disable-news-notifications)))

(rf/reg-fx
 :effects/push-notifications-enable
 (fn [settings]
   (enable-push-notifications settings)))

(rf/reg-fx
 :effects/push-notifications-disable
 (fn [settings]
   (if platform/android?
     (disable-android-notifications settings)
     (disable-ios-notifications settings))))

(rf/reg-fx
 :effects/push-notifications-clear-message-notifications
 (fn [chat-ids]
   (if platform/android?
     (doseq [chat-id chat-ids]
       (native-module.pn/clear-message-notifications chat-id))
     (pn-notifications/clear-received-notifications))))

(rf/reg-fx :effects/check-notifications-permissions
 (fn [{:keys [on-success on-error]}]
   (-> (pn-permissions/check-notification-permissions)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx :effects/open-notifications-settings
 (fn []
   (pn-permissions/open-notifications-settings)))
