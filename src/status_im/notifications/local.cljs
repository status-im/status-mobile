(ns status-im.notifications.local
  (:require [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.decode :as decode]
            ["@react-native-community/push-notification-ios" :default pn-ios]
            [status-im.notifications.android :as pn-android]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [status-im.utils.money :as money]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n.i18n :as i18n]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [cljs-bean.core :as bean]
            [status-im.ui.screens.chat.components.reply :as reply]
            [clojure.string :as clojure.string]
            [status-im.chat.models :as chat.models]
            [status-im.constants :as constants]))

(def default-erc20-token
  {:symbol   :ERC20
   :decimals 18
   :name     "ERC20"})

(def notification-event-ios "localNotification")
(def notification-event-android "remoteNotificationReceived")

(defn local-push-ios [{:keys [title message user-info body-type]}]
  (when (not= body-type "message")
    (.presentLocalNotification
     pn-ios
     #js {:alertBody  message
          :alertTitle title
          ;; NOTE: Use a special type to hide in Obj-C code other notifications
          :userInfo   (bean/->js (merge user-info
                                        {:notificationType "local-notification"}))})))

(defn local-push-android
  [{:keys [title message icon user-info channel-id type]
    :as   notification
    :or   {channel-id "status-im-notifications"}}]
  (pn-android/present-local-notification
   (merge {:channelId channel-id
           :title     title
           :message   message
           :showBadge false}
          (when user-info
            {:userInfo (bean/->js user-info)})
          (when icon
            {:largeIconUrl (:uri (react/resolve-asset-source icon))})
          (when (= type "message")
            notification))))

(defn handle-notification-press [{{deep-link :deepLink} :userInfo
                                  interaction           :userInteraction}]
  (when (and deep-link
             (or platform/ios?
                 (and platform/android? interaction)))
    (re-frame/dispatch [:universal-links/handle-url deep-link])))

(defn listen-notifications []
  (if platform/ios?
    (.addEventListener ^js pn-ios
                       notification-event-ios
                       (fn [notification]
                         (handle-notification-press {:userInfo (bean/bean (.getData ^js notification))})))
    (.addListener ^js react/device-event-emitter
                  notification-event-android
                  (fn [^js data]
                    (when (and data (.-dataJSON data))
                      (handle-notification-press (types/json->clj (.-dataJSON data))))))))

(defn create-transfer-notification
  [{{:keys [state from to fromAccount toAccount value erc20 contract network]}
    :body
    :as notification}]
  (let [chain       (ethereum/chain-id->chain-keyword network)
        token       (if erc20
                      (get-in tokens/all-tokens-normalized
                              [(keyword chain) (clojure.string/lower-case contract)]
                              default-erc20-token)
                      (tokens/native-currency (keyword chain)))
        amount      (money/wei->ether (decode/uint value))
        to          (or (:name toAccount) (utils/get-shortened-address to))
        from        (or (:name fromAccount) (utils/get-shortened-address from))
        title       (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction {:value    amount
                                                                          :currency (:symbol token)})
                      "outbound" (i18n/label :t/push-outbound-transaction {:value    amount
                                                                           :currency (:symbol token)})
                      "failed"   (i18n/label :t/push-failed-transaction {:value    amount
                                                                         :currency (:symbol token)})
                      nil)
        description (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction-body {:from from
                                                                               :to   to})
                      "outbound" (i18n/label :t/push-outbound-transaction-body {:from from
                                                                                :to   to})
                      "failed"   (i18n/label :t/push-failed-transaction-body {:value    amount
                                                                              :currency (:symbol token)
                                                                              :to       to})
                      nil)]
    {:title     title
     :icon      (get-in token [:icon :source])
     :user-info notification
     :message   description}))

(defn show-message-pn?
  [{{:keys [app-state multiaccount]} :db :as cofx}
   {{:keys [message chat]} :body}]
  (let [chat-id (get chat :id)
        chat-type (get chat :chatType)]
    (and
     (or (= app-state "background")
         (not (chat.models/foreground-chat? cofx chat-id)))
     (or (contains? #{constants/one-to-one-chat-type
                      constants/private-group-chat-type}
                    chat-type)
         (contains? (set (get message :mentions))
                    (get multiaccount :public-key))))))

(defn create-message-notification
  ([cofx notification]
   (when (or (nil? cofx)
             (show-message-pn? cofx notification))
     (create-message-notification notification)))
  ([{{:keys [message contact chat]} :body}]
   (let [chat-type    (get chat :chatType)
         chat-id      (get chat :id)
         contact-name @(re-frame/subscribe
                        [:contacts/contact-name-by-identity (get contact :id)])
         group-chat?  (not= chat-type constants/one-to-one-chat-type)
         title        (clojure.string/join
                       " "
                       (cond-> [contact-name]
                         group-chat?
                         (conj
                          ;; TODO(rasom): to be translated
                          "in")

                         group-chat?
                         (conj
                          (str (when (contains? #{constants/public-chat-type
                                                  constants/community-chat-type}
                                                chat-type)
                                 "#")
                               (get chat :name)))))]
     {:type             "message"
      :chatType         (str (get chat :chatType))
      :from             title
      :chatId           chat-id
      :alias            title
      :identicon        (get contact :identicon)
      :whisperTimestamp (get message :whisperTimestamp)
      :text             (reply/get-quoted-text-with-mentions (:parsedText message))})))

(defn create-notification
  ([notification]
   (create-notification nil notification))
  ([cofx {:keys [bodyType] :as notification}]
   (assoc
    (case bodyType
      "message"     (create-message-notification cofx notification)
      "transaction" (create-transfer-notification notification)
      nil)
    :body-type bodyType)))

(re-frame/reg-fx
 ::local-push-ios
 (fn [evt]
   (-> evt create-notification local-push-ios)))

(fx/defn local-notification-android
  {:events [::local-notification-android]}
  [cofx event]
  (some->> event
           (create-notification cofx)
           local-push-android))

(fx/defn process
  [cofx evt]
  (if platform/ios?
    {::local-push-ios evt}
    (local-notification-android cofx evt)))

(defn handle []
  (fn [^js message]
    (let [evt (types/json->clj (.-event message))]
      (js/Promise.
       (fn [on-success on-error]
         (try
           (when (= "local-notifications" (:type evt))
             (re-frame/dispatch [::local-notification-android (:event evt)]))
           (on-success)
           (catch :default e
             (log/warn "failed to handle background notification" e)
             (on-error e))))))))
