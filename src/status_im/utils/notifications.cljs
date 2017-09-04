(ns status-im.utils.notifications
  (:require [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.components.react :refer [copy-to-clipboard]]
            [taoensso.timbre :as log]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

;; NOTE: Only need to explicitly request permissions on iOS.
(defn request-permissions []
  (.requestPermissions (.-default rn/react-native-fcm)))

(defn get-fcm-token []
    (-> (.getFCMToken (aget rn/react-native-fcm "default"))
        (.then (fn [x]
                 (when config/notifications-wip-enabled?
                   (log/info "FCM token" x)
                   (copy-to-clipboard x)
                   (utils/show-popup "INFO" (str "FCM Token in clipboard: " x)))))))

(defn on-refresh-fcm-token []
  (.on (.-default rn/react-native-fcm)
       (.-RefreshToken (.-FCMEvent rn/react-native-fcm))
       (fn [x]
         (log/info "FCM token refreshed" x)
         (copy-to-clipboard x)
         (utils/show-popup "INFO" (str "FCM token (refreshed) in clipboard: " x)))))
