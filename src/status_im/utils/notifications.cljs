(ns status-im.utils.notifications
  (:require [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.utils.handlers :as handlers]
            [status-im.components.status :as status]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.components.react :refer [copy-to-clipboard]]
            [taoensso.timbre :as log]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

(handlers/register-handler-db
 :update-fcm-token
 (fn [{:keys [accounts/current-account-id] :as db} [_ fcm-token]]
   (log/info "***update-fcm-token" fcm-token)
   (-> db
       (assoc-in [:accounts/accounts current-account-id :fcm-token] fcm-token))))

;; NOTE: For easy testing of notifications bindings/interface in REPL
(defn test-notify []
  (status/notify "foobar"
               (fn [res]
                 (println "*** NOTIFY RESULT" res))))

;; NOTE: Only need to explicitly request permissions on iOS.
(defn request-permissions []
  (.requestPermissions (.-default rn/react-native-fcm)))

;; this happening too early? need accounts init.
;; ffs
;; I guess it brlongs to device GAH
;; cause we get it first wso we need it there
;; means more meta thing, then take it down somehow
;; oh well not crashing right now so ok
(defn get-fcm-token []
    (-> (.getFCMToken (aget rn/react-native-fcm "default"))
        (.then (fn [x]
                 (when config/notifications-wip-enabled?
                   (log/info "FCM token" x)
                   (dispatch [:update-fcm-token x])
                   #_(copy-to-clipboard x)
                   #_(utils/show-popup "INFO" (str "FCM Token in clipboard: " x)))))))

(defn on-refresh-fcm-token []
  (.on (.-default rn/react-native-fcm)
       (.-RefreshToken (.-FCMEvent rn/react-native-fcm))
       (fn [x]
         (log/info "FCM token refreshed" x)
         (dispatch [:update-fcm-token x])
         #_(copy-to-clipboard x)
         #_(utils/show-popup "INFO" (str "FCM token (refreshed) in clipboard: " x)))))
