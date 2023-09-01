(ns status-im.notifications.core
  (:require ["@react-native-community/push-notification-ios" :default pn-ios]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.notifications.android :as pn-android]
            [status-im.notifications.local :as local]
            [status-im2.config :as config]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(def server-type-default 1)
(def server-type-custom 2)

(def apn-token-type 1)
(def firebase-token-type 2)
(def listeners-added? (atom nil))
(defn server<-rpc
  [{:keys [type publicKey registered]}]
  {:public-key publicKey
   :type       type
   :registered registered})

(defn add-event-listeners
  []
  (when-not @listeners-added?
    (reset! listeners-added? true)
    (.addEventListener
     ^js pn-ios
     "register"
     (fn [token]
       (re-frame/dispatch [:notifications/registered-for-push-notifications token])))
    (.addEventListener
     ^js pn-ios
     "registrationError"
     (fn [error]
       (re-frame/dispatch [:notifications/switch-error true error])))))

(defn enable-ios-notifications
  []
  (add-event-listeners)
  (-> (.requestPermissions ^js pn-ios)
      (.then #())
      (.catch #())))

(defn disable-ios-notifications
  []
  (.abandonPermissions ^js pn-ios)
  (re-frame/dispatch [:notifications/unregistered-from-push-notifications]))

(defn enable-android-notifications
  []
  (pn-android/create-channel
   {:channel-id   "status-im-notifications"
    :channel-name "Status push notifications"})
  (pn-android/enable-notifications))

(defn disable-android-notifications
  []
  (pn-android/disable-notifications))

;; FIXME: Repalce with request permission from audio messages PR lib
(re-frame/reg-fx
 ::request-permission
 identity)

(rf/defn request-permission
  {:events [::request-permission]}
  [_]
  {::request-permission true})

(re-frame/reg-fx
 ::local-notification
 (fn [props]
   (if platform/ios?
     (local/local-push-ios props)
     (local/local-push-android props))))

(re-frame/reg-fx
 ::enable
 (fn []
   (if platform/android?
     (enable-android-notifications)
     (enable-ios-notifications))))

(re-frame/reg-fx
 ::disable
 (fn [_]
   (if platform/android?
     (disable-android-notifications)
     (disable-ios-notifications))))

(re-frame/reg-fx
 ::logout-disable
 (fn [_]
   (if platform/android?
     (pn-android/disable-notifications)
     (.abandonPermissions ^js pn-ios))))

(re-frame/reg-fx
 :clear-message-notifications
 (fn [[chat-ids] remote-push-notifications-enabled?]
   (if remote-push-notifications-enabled?
     (if platform/android?
       (pn-android/clear-all-message-notifications)
       (.removeAllDeliveredNotifications ^js pn-ios))
     (when platform/android?
       (doseq [chat-id chat-ids]
         (pn-android/clear-message-notifications chat-id))))))

(rf/defn handle-enable-notifications-event
  {:events [:notifications/registered-for-push-notifications]}
  [cofx token]
  {:json-rpc/call [{:method     "wakuext_registerForPushNotifications"
                    :params     [token (if platform/ios? config/apn-topic)
                                 (if platform/ios? apn-token-type firebase-token-type)]
                    :on-success #(log/info "[push-notifications] register-success" %)
                    :on-error   #(re-frame/dispatch [:notifications/switch-error true %])}]})

(rf/defn handle-disable-notifications-event
  {:events [:notifications/unregistered-from-push-notifications]}
  [cofx]
  {:json-rpc/call [{:method     "wakuext_unregisterFromPushNotifications"
                    :params     []
                    :on-success #(log/info "[push-notifications] unregister-success" %)
                    :on-error   #(re-frame/dispatch [:notifications/switch-error false %])}]})

(rf/defn logout-disable
  [cofx]
  (merge {::logout-disable nil}
         {:json-rpc/call [{:method     "wakuext_unregisterFromPushNotifications"
                           :params     []
                           :on-success #(log/info "[push-notifications] unregister-success" %)
                           :on-error   #(log/info "[push-notifications] unregister-error" %)}]}))

(rf/defn notification-switch-error
  {:events [:notifications/switch-error]}
  [cofx enabled?]
  (multiaccounts.update/multiaccount-update
   cofx
   :remote-push-notifications-enabled?
   (not enabled?)
   {}))

(rf/defn notification-switch
  {:events [::switch]}
  [{:keys [db] :as cofx} enabled? remote-push-notifications?]
  (rf/merge cofx
            (if enabled?
              {::enable remote-push-notifications?}
              {::disable nil})
            (multiaccounts.update/multiaccount-update
             :remote-push-notifications-enabled?
             (and remote-push-notifications? enabled?)
             {})
            (multiaccounts.update/multiaccount-update
             :notifications-enabled?
             (and (not remote-push-notifications?) enabled?)
             {})))

(rf/defn notification-non-contacts-error
  {:events [::non-contacts-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx
                                   :push-notifications-from-contacts-only?
                                   (not (boolean enabled?))))

(rf/defn notification-block-mentions-error
  {:events [::block-mentions-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx :push-notifications-block-mentions? (not (boolean enabled?))))

(rf/defn notification-non-contacts
  {:events [::switch-non-contacts]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_enablePushNotificationsFromContactsOnly"
                 "wakuext_disablePushNotificationsFromContactsOnly")]
    (rf/merge
     cofx
     {:json-rpc/call [{:method     method
                       :params     []
                       :on-success #(log/info "[push-notifications] contacts-notification-success" %)
                       :on-error   #(re-frame/dispatch [::non-contacts-update-error enabled? %])}]}

     (multiaccounts.update/optimistic :push-notifications-from-contacts-only? (boolean enabled?)))))

(rf/defn notification-block-mentions
  {:events [::switch-block-mentions]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_enablePushNotificationsBlockMentions"
                 "wakuext_disablePushNotificationsBlockMentions")]
    (log/info "USING METHOD" method enabled?)
    (rf/merge cofx
              {:json-rpc/call [{:method     method
                                :params     []
                                :on-success #(log/info "[push-notifications] block-mentions-success" %)
                                :on-error   #(re-frame/dispatch [::block-mentions-update-error enabled?
                                                                 %])}]}

              (multiaccounts.update/optimistic :push-notifications-block-mentions? (boolean enabled?)))))

(rf/defn switch-push-notifications-server-enabled
  {:events [::switch-push-notifications-server-enabled]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_startPushNotificationsServer"
                 "wakuext_stopPushNotificationsServer")]
    (rf/merge
     cofx
     {:json-rpc/call [{:method     method
                       :params     []
                       :on-success #(log/info "[push-notifications] switch-server-enabled successful" %)
                       :on-error   #(re-frame/dispatch [::push-notifications-server-update-error
                                                        enabled? %])}]}

     (multiaccounts.update/optimistic :push-notifications-server-enabled? (boolean enabled?)))))

(rf/defn switch-send-notifications
  {:events [::switch-send-push-notifications]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "wakuext_enableSendingNotifications"
                 "wakuext_disableSendingNotifications")]
    (rf/merge cofx
              {:json-rpc/call [{:method method
                                :params []
                                :on-success
                                #(log/info "[push-notifications] switch-send-notifications successful"
                                           %)
                                :on-error #(re-frame/dispatch [::push-notifications-send-update-error
                                                               enabled? %])}]}

              (multiaccounts.update/optimistic :send-push-notifications? (boolean enabled?)))))

(rf/defn handle-add-server-error
  {:events [::push-notifications-add-server-error]}
  [_ public-key error]
  (log/error "failed to add server" public-key error))

(rf/defn add-server
  {:events [::add-server]}
  [{:keys [db] :as cofx} public-key]
  (rf/merge cofx
            {:json-rpc/call [{:method "wakuext_addPushNotificationsServer"
                              :params [public-key]
                              :on-success
                              #(do
                                 (log/info "[push-notifications] switch-send-notifications successful"
                                           %)
                                 (re-frame/dispatch [::fetch-servers]))
                              :on-error #(re-frame/dispatch [::push-notifications-add-server-error
                                                             public-key %])}]}))

(rf/defn handle-servers-fetched
  {:events [::servers-fetched]}
  [{:keys [db]} servers]
  {:db (assoc db :push-notifications/servers (map server<-rpc servers))})

(rf/defn fetch-push-notifications-servers
  {:events [::fetch-servers]}
  [cofx]
  {:json-rpc/call [{:method     "wakuext_getPushNotificationsServers"
                    :params     []
                    :on-success #(do
                                   (log/info "[push-notifications] servers fetched" %)
                                   (re-frame/dispatch [::servers-fetched %]))}]})

;; Wallet transactions

(rf/defn handle-preferences-load
  {:events [::preferences-loaded]}
  [{:keys [db]} preferences]
  {:db (assoc db :push-notifications/preferences preferences)})

(rf/defn load-notification-preferences
  {:events [::load-notification-preferences]}
  [_]
  {:json-rpc/call [{:method     "localnotifications_notificationPreferences"
                    :params     []
                    :on-success #(re-frame/dispatch [::preferences-loaded %])}]})

(defn preference=
  [x y]
  (and (= (:service x) (:service y))
       (= (:event x) (:event y))
       (= (:identifier x) (:identifier y))))

(defn- update-preference
  [all new-preference]
  (conj (filter (comp not (partial preference= new-preference))
                all)
        new-preference))

(rf/defn switch-transaction-notifications
  {:events [::switch-transaction-notifications]}
  [{:keys [db] :as cofx} enabled?]
  {:db            (update db
                          :push-notifications/preferences
                          update-preference
                          {:enabled    (not enabled?)
                           :service    "wallet"
                           :event      "transaction"
                           :identifier "all"})
   :json-rpc/call [{:method     "localnotifications_switchWalletNotifications"
                    :params     [(not enabled?)]
                    :on-success #(log/info
                                  "[push-notifications] switch-transaction-notifications successful"
                                  %)
                    :on-error   #(log/error
                                  "[push-notifications] switch-transaction-notifications error"
                                  %)}]})
