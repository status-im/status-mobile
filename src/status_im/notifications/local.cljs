(ns status-im.notifications.local
  (:require ["@react-native-community/push-notification-ios" :default pn-ios]
            [cljs-bean.core :as bean]
            [clojure.string :as string]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.async-storage.core :as async-storage]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.tokens :as tokens]
            [utils.i18n :as i18n]
            [status-im.notifications.android :as pn-android]
            [utils.re-frame :as rf]
            [utils.money :as money]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [react-native.core :as rn]))

(def default-erc20-token
  {:symbol   :ERC20
   :decimals 18
   :name     "ERC20"})

(def notification-event-ios "localNotification")
(def notification-event-android "remoteNotificationReceived")

(defn local-push-ios
  [{:keys [title message user-info body-type]}]
  (when (not= body-type "message")
    (.presentLocalNotification
     pn-ios
     #js
      {:alertBody  message
       :alertTitle title
       ;; NOTE: Use a special type to hide in Obj-C code other notifications
       :userInfo   (bean/->js (merge user-info
                                     {:notificationType "local-notification"}))})))

(defn local-push-android
  [notification]
  (pn-android/present-local-notification notification))

(defn handle-notification-press
  [{{deep-link :deepLink} :userInfo
    interaction           :userInteraction}]
  (async-storage/set-item! (str :chat-id) nil)
  (when (and deep-link
             (or platform/ios?
                 (and platform/android? interaction)))
    (re-frame/dispatch [:universal-links/handle-url deep-link])))

(defn listen-notifications
  []
  (if platform/ios?
    (.addEventListener ^js pn-ios
                       notification-event-ios
                       (fn [notification]
                         (handle-notification-press {:userInfo (bean/bean (.getData ^js
                                                                                    notification))})))
    (.addListener ^js rn/device-event-emitter
                  notification-event-android
                  (fn [^js data]
                    (when (and data (.-dataJSON data))
                      (handle-notification-press (types/json->clj (.-dataJSON data))))))))

(defn create-transfer-notification
  [{db :db}
   {{:keys [state from to fromAccount toAccount value erc20 contract network]}
    :body
    :as notification}]
  (let [token       (if erc20
                      (get-in db
                              [:wallet/all-tokens (string/lower-case contract)]
                              default-erc20-token)
                      (tokens/native-currency network))
        amount      (money/wei->ether (decode/uint value))
        to          (or (:name toAccount) (utils/get-shortened-address to))
        from        (or (:name fromAccount) (utils/get-shortened-address from))
        title       (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      "outbound" (i18n/label :t/push-outbound-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      "failed"   (i18n/label :t/push-failed-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      nil)
        description (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction-body
                                             {:from from
                                              :to   to})
                      "outbound" (i18n/label :t/push-outbound-transaction-body
                                             {:from from
                                              :to   to})
                      "failed"   (i18n/label :t/push-failed-transaction-body
                                             {:value    amount
                                              :currency (:symbol token)
                                              :to       to})
                      nil)]
    {:title     title
     :icon      (get-in token [:icon :source])
     :deepLink  (:deepLink notification)
     :user-info notification
     :message   description}))

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
  ([notification]
   (create-notification nil notification))
  ([cofx {:keys [bodyType] :as notification}]
   (assoc
    (case bodyType
      "message"     (when (show-message-pn? cofx notification) notification)
      "transaction" (create-transfer-notification cofx notification)
      nil)
    :body-type
    bodyType)))

(re-frame/reg-fx
 ::local-push-ios
 (fn [evt]
   (-> evt create-notification local-push-ios)))

(rf/defn local-notification-android
  {:events [::local-notification-android]}
  [cofx event]
  (some->> event
           (create-notification cofx)
           local-push-android))

(rf/defn process
  [cofx evt]
  (if platform/ios?
    {::local-push-ios evt}
    (local-notification-android cofx evt)))
