(ns status-im.contexts.profile.push-notifications.effects
  (:require
    [native-module.push-notifications :as native-module.pn]
    [react-native.platform :as platform]
    [react-native.push-notification-ios :as pn-ios]
    [utils.re-frame :as rf]))

(def ios-listeners-added? (atom nil))

(defn enable-ios-notifications
  []
  (when-not @ios-listeners-added?
    (reset! ios-listeners-added? true)
    (pn-ios/add-listener
     "register"
     (fn [token]
       (rf/dispatch [:push-notifications/registered-for-push-notifications token])))
    (pn-ios/add-listener
     "registrationError"
     (fn [error]
       (rf/dispatch [:push-notifications/switch-error true error]))))
  (pn-ios/request-permissions))

(defn disable-ios-notifications
  []
  (pn-ios/abandon-permissions)
  (rf/dispatch [:push-notifications/unregistered-from-push-notifications]))

(defn enable-android-notifications
  []
  (native-module.pn/create-channel
   {:channel-id   "status-im-notifications"
    :channel-name "Status push notifications"})
  (native-module.pn/enable-notifications))

(defn disable-android-notifications
  []
  (native-module.pn/disable-notifications))

(rf/reg-fx
 :effects/push-notifications-enable
 (fn []
   (if platform/android?
     (enable-android-notifications)
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
     (pn-ios/remove-all-delivered-notifications))))
