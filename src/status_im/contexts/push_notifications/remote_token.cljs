(ns status-im.contexts.push-notifications.remote-token
  (:require
    [promesa.core :as promesa]
    [react-native.firebase :as firebase]
    [react-native.platform :as platform]
    [react-native.push-notification-ios :as pn-ios]))

(defn request-remote-token
  "Request the remote token for the mobile platform.
   On iOS this will retrieve a APNS token,
   and on Android this will retrieve a FCM token."
  [options]
  (-> (if platform/ios?
          (pn-ios/request-remote-token options)
          (firebase/request-remote-token options))
      (promesa/then (fn [token] {:token token}))))

(defn request-fcm-token
  [options]
  (-> (firebase/request-remote-token options)
      (promesa/then (fn [token] {:fcm-token token}))))

(defn revoke-fcm-token
  [options]
  (firebase/revoke-remote-token options))

(defn request-apns-token
  [options]
  (-> (pn-ios/request-remote-token options)
      (promesa/then (fn [token] {:apns-token token}))))

(defn request-apns-and-fcm-token
  [options]
  (-> (promesa/all [(request-apns-token options)
                    (request-fcm-token options)])
      (promesa/then
       (fn [labeled-tokens]
         (apply merge labeled-tokens)))))

(defn select-platform-token
  [tokens]
  (cond
    platform/ios?     (:apns-token tokens)
    platform/android? (:fcm-token tokens)
    :else             (:token tokens)))
