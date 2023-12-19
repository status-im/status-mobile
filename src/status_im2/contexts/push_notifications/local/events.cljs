(ns status-im2.contexts.push-notifications.local.events
  (:require
    [legacy.status-im.notifications.wallet :as notifications.wallet]
    [react-native.platform :as platform]
    status-im2.contexts.push-notifications.local.effects
    [utils.re-frame :as rf]))


(defn foreground-chat?
  [{{:keys [current-chat-id view-id]} :db} chat-id]
  (and (= current-chat-id chat-id)
       (= view-id :chat)))

(defn show-message-pn?
  [{{:keys [app-state profile/profile]} :db :as cofx}
   notification]
  (let [chat-id             (get-in notification [:body :chat :id])
        notification-author (get-in notification [:notificationAuthor :id])]
    (and
     (not= notification-author (:public-key profile))
     (or (= app-state "background")
         (not (foreground-chat? cofx chat-id))))))

(defn create-notification
  [cofx {:keys [bodyType] :as notification}]
  (assoc
   (case bodyType
     "message"     (when (show-message-pn? cofx notification) notification)
     "transaction" (notifications.wallet/create-transfer-notification cofx notification)
     nil)
   :body-type
   bodyType))

(rf/defn process
  [cofx event]
  (if platform/ios?
    {:effects/push-notifications-local-present-ios (create-notification nil event)}
    {:effects/push-notifications-local-present-android (create-notification cofx event)}))
