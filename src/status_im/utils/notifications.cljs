(ns status-im.utils.notifications
  (:require [status-im.react-native.js-dependencies :as rn]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

(defn get-fcm-token []
  (let [fcm (aget rn/react-native-fcm "default")]
    (.then ((.-getFCMToken fcm))
           (fn [x] (println "*** FCM TOKEN" x)))))
