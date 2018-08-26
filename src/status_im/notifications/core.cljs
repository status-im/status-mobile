(ns status-im.notifications.core
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.handlers :as handlers]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.ui.components.react :refer [copy-to-clipboard]]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

(when-not platform/desktop?

  (def firebase (object/get rn/react-native-firebase "default"))

  ;; NOTE: Only need to explicitly request permissions on iOS.
  (defn request-permissions []
    (if platform/desktop?
      (re-frame/dispatch [:notifications/request-notifications-granted {}])
      (-> (.requestPermission (.messaging firebase))
          (.then
           (fn [_]
             (log/debug "notifications-granted")
             (re-frame/dispatch [:notifications/request-notifications-granted {}]))
           (fn [_]
             (log/debug "notifications-denied")
             (re-frame/dispatch [:notifications/request-notifications-denied {}]))))))

  (defn get-fcm-token []
    (-> (.getToken (.messaging firebase))
        (.then (fn [x]
                 (log/debug "get-fcm-token: " x)
                 (re-frame/dispatch [:notifications/update-fcm-token x])))))

  (defn on-refresh-fcm-token []
    (.onTokenRefresh (.messaging firebase)
                     (fn [x]
                       (log/debug "on-refresh-fcm-token: " x)
                       (re-frame/dispatch [:notifications/update-fcm-token x]))))

  ;; TODO(oskarth): Only called in background on iOS right now.
  ;; NOTE(oskarth): Hardcoded data keys :sum and :msg in status-go right now.
  (defn on-notification []
    (.onNotification (.notifications firebase)
                     (fn [event-js]
                       (let [event (js->clj event-js :keywordize-keys true)
                             data (select-keys event [:sum :msg])
                             aps (:aps event)]
                         (log/debug "on-notification event: " (pr-str event))
                         (log/debug "on-notification aps: " (pr-str aps))
                         (log/debug "on-notification data: " (pr-str data))))))

  (def channel-id "status-im")
  (def channel-name "Status")
  (def sound-name "message.wav")
  (def group-id "im.status.ethereum.MESSAGE")
  (def icon "ic_stat_status_notification")

  (defn create-notification-channel []
    (let [channel (firebase.notifications.Android.Channel. channel-id
                                                           channel-name
                                                           firebase.notifications.Android.Importance.Max)]
      (.setSound channel sound-name)
      (.setShowBadge channel true)
      (.enableVibration channel true)
      (.. firebase
          notifications
          -android
          (createChannel channel)
          (then #(log/debug "Notification channel created:" channel-id)
                #(log/error "Notification channel creation error:" channel-id %)))))

  (defn store-event [{:keys [from to]} {:keys [db] :as cofx}]
    (let [{:keys [address photo-path name]} (->> (get-in cofx [:db :accounts/accounts])
                                                 vals
                                                 (filter #(= (:public-key %) to))
                                                 first)]
      (when address
        {:db       (assoc-in db [:push-notifications/stored to] from)
         :dispatch [:open-login address photo-path name]})))

  (defn process-initial-push-notification [{:keys [initial?]} {:keys [db]}]
    (when initial?
      {:db (assoc db :push-notifications/initial? true)}))

  (defn process-push-notification [{:keys [from to] :as event} {:keys [db] :as cofx}]
    (let [current-public-key (get-in cofx [:db :current-public-key])]
      (if current-public-key
        (when (= to current-public-key)
          {:db       (update db :push-notifications/stored dissoc to)
           :dispatch [:navigate-to-chat from]})
        (store-event event cofx))))

  (defn handle-push-notification
    [cofx [_ event]]
    (handlers-macro/merge-fx cofx
                             (process-initial-push-notification event)
                             (process-push-notification event)))

  (defn stored-event [address cofx]
    (let [to (get-in cofx [:db :accounts/accounts address :public-key])
          from (get-in cofx [:db :push-notifications/stored to])]
      (when from
        [:notification/handle-push-notification {:from from
                                                 :to   to}])))

  (defn parse-notification-payload [s]
    (try
      (js/JSON.parse s)
      (catch :default _
        #js {})))

  (defn handle-notification-event [event {:keys [initial?]}]
    (let [msg (object/get (.. event -notification -data) "msg")
          data (parse-notification-payload msg)
          from (object/get data "from")
          to (object/get data "to")]
      (log/debug "on notification" (pr-str msg))
      (when (and from to)
        (re-frame/dispatch [:notification/handle-push-notification {:from     from
                                                                    :to       to
                                                                    :initial? initial?}]))))

  (defn handle-initial-push-notification
    [initial?]
    (when-not initial?
      (.. firebase
          notifications
          getInitialNotification
          (then (fn [event]
                  (when event
                    (handle-notification-event event {:initial? true})))))))

  (defn on-notification-opened []
    (.. firebase
        notifications
        (onNotificationOpened handle-notification-event)))

  (def notification (firebase.notifications.Notification.))

  ;; API reference https://rnfirebase.io/docs/v4.2.x/notifications/reference/AndroidNotification
  (defn display-notification [{:keys [title body from to]}]
    (.. notification
        (setTitle title)
        (setBody body)
        (setData (js/JSON.stringify #js {:from from
                                         :to   to}))
        (setSound sound-name)
        (-android.setChannelId channel-id)
        (-android.setAutoCancel true)
        (-android.setPriority firebase.notifications.Android.Priority.Max)
        (-android.setGroup group-id)
        (-android.setGroupSummary true)
        (-android.setSmallIcon icon))
    (.. firebase
        notifications
        (displayNotification notification)
        (then #(log/debug "Display Notification" title body))
        (then #(log/debug "Display Notification error" title body))))

  (defn init []
    (on-refresh-fcm-token)
    (on-notification)
    (on-notification-opened)
    (when platform/android?
      (create-notification-channel))))
