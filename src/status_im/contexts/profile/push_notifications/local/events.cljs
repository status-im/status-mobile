(ns status-im.contexts.profile.push-notifications.local.events
  (:require
    [legacy.status-im.notifications.wallet :as notifications.wallet]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    status-im.contexts.profile.push-notifications.local.effects
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private push-notification-types
  #{"transaction" "message"})

(defn foreground-chat?
  [{{:keys [current-chat-id view-id]} :db} chat-id]
  (and (= current-chat-id chat-id)
       (= view-id :screen/chat)))

(defn show-message-pn?
  [{{:keys [app-state profile/profile]} :db :as cofx}
   notification]
  (let [chat-id             (get-in notification [:body :chat :id])
        notification-author (get-in notification [:notificationAuthor :id])]
    (and
     (not= notification-author (:public-key profile))
     (or (= app-state "background")
         (not (foreground-chat? cofx chat-id))))))

(defn show-community-joined-toast
  [{:keys [bodyType title]}]
  (when (= bodyType constants/community-joined-notification-type)
    {:dispatch [:toasts/upsert
                {:id   :joined-community
                 :type :positive
                 :text (i18n/label :t/joined-community {:community title})}]}))

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
  [cofx {:keys [bodyType] :as event}]
  (if (push-notification-types bodyType)
    (if platform/ios?
      {:effects/push-notifications-local-present-ios (create-notification nil event)}
      {:effects/push-notifications-local-present-android (create-notification cofx event)})
    (show-community-joined-toast event)))
