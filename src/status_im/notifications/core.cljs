(ns status-im.notifications.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            ["@react-native-community/push-notification-ios" :default pn-ios]
            [status-im.notifications.android :as pn-android]
            [status-im.native-module.core :as status]
            [status-im.notifications.local :as local]
            [quo.platform :as platform]
            [status-im.utils.config :as config]
            [status-im.ethereum.json-rpc :as json-rpc]))

(def server-type-default 1)
(def server-type-custom 2)

(def apn-token-type 1)
(def firebase-token-type 2)
(def listeners-added? (atom nil))
(defn server<-rpc [{:keys [type publicKey registered]}]
  {:public-key publicKey
   :type type
   :registered registered})

(defn add-event-listeners []
  (when-not @listeners-added?
    (reset! listeners-added? true)
    (.addEventListener
     ^js pn-ios
     "register"
     (fn [token]
       (re-frame/dispatch [::registered-for-push-notifications token])))
    (.addEventListener
     ^js pn-ios
     "registrationError"
     (fn [error]
       (re-frame/dispatch [::switch-error true error])))))

(defn enable-ios-notifications []
  (add-event-listeners)
  (-> (.requestPermissions ^js pn-ios)
      (.then #())
      (.catch #())))

(defn disable-ios-notifications []
  (.abandonPermissions ^js pn-ios)
  (re-frame/dispatch [::unregistered-from-push-notifications]))

;; FIXME: Repalce with request permission from audio messages PR lib
(re-frame/reg-fx
 ::request-permission
 identity)

(fx/defn request-permission
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
 (fn [_]
   (if platform/android?
     (do
       (pn-android/create-channel {:channel-id   "status-im-notifications"
                                   :channel-name "Status push notifications"})
       (status/enable-notifications))
     (enable-ios-notifications))))

(re-frame/reg-fx
 ::disable
 (fn [_]
   (if platform/android?
     (status/disable-notifications)
     (disable-ios-notifications))))

(re-frame/reg-fx
 ::logout-disable
 (fn [_]
   (if platform/android?
     (status/disable-notifications)
     (.abandonPermissions ^js pn-ios))))

(fx/defn handle-enable-notifications-event
  {:events [::registered-for-push-notifications]}
  [cofx token]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "registerForPushNotifications")
                     :params     [token (if platform/ios? config/apn-topic) (if platform/ios? apn-token-type firebase-token-type)]
                     :on-success #(log/info "[push-notifications] register-success" %)
                     :on-error   #(re-frame/dispatch [::switch-error true %])}]})

(fx/defn handle-disable-notifications-event
  {:events [::unregistered-from-push-notifications]}
  [cofx]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "unregisterFromPushNotifications")
                     :params     []
                     :on-success #(log/info "[push-notifications] unregister-success" %)
                     :on-error   #(re-frame/dispatch [::switch-error false %])}]})

(fx/defn logout-disable
  [cofx]
  (merge {::logout-disable nil}
         (when platform/ios?
           {::json-rpc/call [{:method     (json-rpc/call-ext-method "unregisterFromPushNotifications")
                              :params     []
                              :on-success #(log/info "[push-notifications] unregister-success" %)
                              :on-error   #(log/info "[push-notifications] unregister-error" %)}]})))

(fx/defn notification-switch-error
  {:events [::switch-error]}
  [cofx enabled?]
  (if platform/android?
    (multiaccounts.update/multiaccount-update
     :notifications-enabled? (not enabled?)
     {})
    (multiaccounts.update/optimistic cofx :remote-push-notifications-enabled? (not (boolean enabled?)))))

(fx/defn notification-switch
  {:events [::switch]}
  [{:keys [db] :as cofx} enabled?]
  (fx/merge cofx
            (if enabled?
              {::enable nil}
              {::disable nil})
            (if platform/android?
              (multiaccounts.update/multiaccount-update
               :notifications-enabled? enabled?
               {})
              (multiaccounts.update/optimistic :remote-push-notifications-enabled? (boolean enabled?)))))

(fx/defn notification-non-contacts-error
  {:events [::non-contacts-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx :push-notifications-from-contacts-only? (not (boolean enabled?))))

(fx/defn notification-block-mentions-error
  {:events [::block-mentions-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic cofx :push-notifications-block-mentions? (not (boolean enabled?))))

(fx/defn notification-non-contacts
  {:events [::switch-non-contacts]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "enablePushNotificationsFromContactsOnly"
                 "disablePushNotificationsFromContactsOnly")]
    (fx/merge cofx
              {::json-rpc/call [{:method     (json-rpc/call-ext-method method)
                                 :params     []
                                 :on-success #(log/info "[push-notifications] contacts-notification-success" %)
                                 :on-error   #(re-frame/dispatch [::non-contacts-update-error enabled? %])}]}

              (multiaccounts.update/optimistic :push-notifications-from-contacts-only? (boolean enabled?)))))

(fx/defn notification-block-mentions
  {:events [::switch-block-mentions]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "enablePushNotificationsBlockMentions"
                 "disablePushNotificationsBlockMentions")]
    (log/info "USING METHOD" method enabled?)
    (fx/merge cofx
              {::json-rpc/call [{:method     (json-rpc/call-ext-method method)
                                 :params     []
                                 :on-success #(log/info "[push-notifications] block-mentions-success" %)
                                 :on-error   #(re-frame/dispatch [::block-mentions-update-error enabled? %])}]}

              (multiaccounts.update/optimistic :push-notifications-block-mentions? (boolean enabled?)))))

(fx/defn switch-push-notifications-server-enabled
  {:events [::switch-push-notifications-server-enabled]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "startPushNotificationsServer"
                 "stopPushNotificationsServer")]
    (fx/merge cofx
              {::json-rpc/call [{:method     (json-rpc/call-ext-method method)
                                 :params     []
                                 :on-success #(log/info "[push-notifications] switch-server-enabled successful" %)
                                 :on-error   #(re-frame/dispatch [::push-notifications-server-update-error enabled? %])}]}

              (multiaccounts.update/optimistic :push-notifications-server-enabled? (boolean enabled?)))))

(fx/defn switch-send-notifications
  {:events [::switch-send-push-notifications]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "enableSendingNotifications"
                 "disableSendingNotifications")]
    (fx/merge cofx
              {::json-rpc/call [{:method     (json-rpc/call-ext-method method)
                                 :params     []
                                 :on-success #(log/info "[push-notifications] switch-send-notifications successful" %)
                                 :on-error   #(re-frame/dispatch [::push-notifications-send-update-error enabled? %])}]}

              (multiaccounts.update/optimistic :send-push-notifications? (boolean enabled?)))))

(fx/defn handle-add-server-error
  {:events [::push-notifications-add-server-error]}
  [_ public-key error]
  (log/error "failed to add server", public-key, error))

(fx/defn add-server
  {:events [::add-server]}
  [{:keys [db] :as cofx} public-key]
  (fx/merge cofx
            {::json-rpc/call [{:method     (json-rpc/call-ext-method "addPushNotificationsServer")
                               :params     [public-key]
                               :on-success #(do
                                              (log/info "[push-notifications] switch-send-notifications successful" %)
                                              (re-frame/dispatch [::fetch-servers]))
                               :on-error   #(re-frame/dispatch [::push-notifications-add-server-error public-key %])}]}))

(fx/defn handle-servers-fetched
  {:events [::servers-fetched]}
  [{:keys [db]} servers]
  {:db (assoc db :push-notifications/servers (map server<-rpc servers))})

(fx/defn fetch-push-notifications-servers
  {:events [::fetch-servers]}
  [cofx]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "getPushNotificationsServers")
                     :params     []
                     :on-success #(do
                                    (log/info "[push-notifications] servers fetched" %)
                                    (re-frame/dispatch [::servers-fetched %]))}]})

;; Wallet transactions

(fx/defn handle-preferences-load
  {:events [::preferences-loaded]}
  [{:keys [db]} preferences]
  {:db (assoc db :push-notifications/preferences preferences)})

(fx/defn load-notification-preferences
  {:events [::load-notification-preferences]}
  [cofx]
  {::json-rpc/call [{:method     "localnotifications_notificationPreferences"
                     :params     []
                     :on-success #(re-frame/dispatch [::preferences-loaded %])}]})

(defn preference= [x y]
  (and (= (:service x) (:service y))
       (= (:event x) (:event y))
       (= (:identifier x) (:identifier y))))

(defn- update-preference [all new]
  (conj (filter (comp not (partial preference= new)) all) new))

(fx/defn switch-transaction-notifications
  {:events [::switch-transaction-notifications]}
  [{:keys [db] :as cofx} enabled?]
  {:db             (update db :push-notifications/preferences update-preference {:enabled    (not enabled?)
                                                                                 :service    "wallet"
                                                                                 :event      "transaction"
                                                                                 :identifier "all"})
   ::json-rpc/call [{:method     "localnotifications_switchWalletNotifications"
                     :params     [(not enabled?)]
                     :on-success #(log/info "[push-notifications] switch-transaction-notifications successful" %)
                     :on-error   #(log/error "[push-notifications] switch-transaction-notifications error" %)}]})
