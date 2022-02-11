(ns status-im.notifications.android-remote
  (:require #?@(:google-free []
                :cljs        [[re-frame.core :as re-frame]
                              [taoensso.timbre :as log]
                              [status-im.notifications.android :as pn-android]
                              ["react-native-notifications" :refer (Notifications)]])))

#?(:google-free
   [(defn register-remote-notifications [] nil)
    (defn unregister-remote-notifications [] nil)]

   :cljs
   [(defonce remote-notifications? (atom false))

    (defn remote-notifications-registered-listener []
      (.registerRemoteNotificationsRegistered
       (.events Notifications)
       (fn [^js evn]
         (let [token (.-deviceToken evn)]
           (when @remote-notifications?
             (re-frame/dispatch [:notifications/registered-for-push-notifications token]))))))

    (defn remote-notifications-registration-failed-listener []
      (.registerRemoteNotificationsRegistrationFailed
       (.events Notifications)
       (fn [^js err]
         (log/info "Remote notifications registration failed" err)
         (reset! @remote-notifications? false)
         (re-frame/dispatch [:notifications/switch-error true err]))))

    (defn notification-opened-listener []
      (.registerNotificationOpened
       (.events Notifications)
       (fn [^js _ cb _]
         (when @remote-notifications?
           (pn-android/clear-all-message-notifications))
         (^js cb))))

    (defn register-remote-notifications []
      (reset! remote-notifications? true)
      ;; event listeners registeration
      (remote-notifications-registered-listener)
      (remote-notifications-registration-failed-listener)
      (notification-opened-listener)
      ;; Register for remote notifications
      (.registerRemoteNotifications Notifications))

    (defn unregister-remote-notifications []
      (reset! remote-notifications? false)
      (re-frame/dispatch [:notifications/unregistered-from-push-notifications]))])
