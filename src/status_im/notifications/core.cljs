(ns status-im.notifications.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as status]
            ["react-native-push-notification" :as rn-pn]
            [quo.platform :as platform]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]))

(defn enable-ios-notifications []
  (.configure
   ^js rn-pn
   #js {:onRegister          (fn [token-data]
                               (let [token (.-token ^js token-data)]
                                 (re-frame/dispatch [::registered-for-push-notifications token])
                                 (println "TOKEN " token)))
        :onRegistrationError (fn [error]
                               (log/error "[push-notifications]" error)
                               (re-frame/dispatch [::switch-error true error]))}))

(defn disable-ios-notifications []
  (.abandonPermissions ^js rn-pn)
  (re-frame/dispatch [::unregistered-from-push-notifications]))

(re-frame/reg-fx
 ::enable
 (fn [_]
   (if platform/android?
     (status/enable-notifications)
     (enable-ios-notifications))))

(re-frame/reg-fx
 ::disable
 (fn [_]
   (if platform/android?
     (status/disable-notifications)
     (disable-ios-notifications))))

(fx/defn handle-enable-notifications-event
  {:events [::registered-for-push-notifications]}
  [cofx token]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "registerForPushNotifications")
                     :params     [token]
                     :on-success #(log/info "[push-notifications] register-success" %)
                     :on-error   #(re-frame/dispatch [::switch-error true %])}]})

(fx/defn handle-disable-notifications-event
  {:events [::unregistered-from-push-notifications]}
  [cofx]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "unregisterForPushNotifications")
                     :params     []
                     :on-success #(log/info "[push-notifications] unregister-success" %)
                     :on-error   #(re-frame/dispatch [::switch-error false %])}]})

(fx/defn notification-switch-error
  {:events [::switch-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic :remote-push-notifications-enabled? (not (boolean enabled?))))

(fx/defn notification-switch
  {:events [::switch]}
  [{:keys [db] :as cofx} enabled?]
  (fx/merge cofx
            (multiaccounts.update/optimistic :remote-push-notifications-enabled? (boolean enabled?))
            (if enabled?
              {::enable nil}
              {::disable nil})))

(fx/defn notification-non-contacts-error
  {:events [::non-contacts-update-error]}
  [cofx enabled?]
  (multiaccounts.update/optimistic :push-notifications-from-contacts-only? (not (boolean enabled?))))

(fx/defn notification-non-contacts
  {:events [::switch-non-contacts]}
  [{:keys [db] :as cofx} enabled?]
  (let [method (if enabled?
                 "disablePushNotificationsFromContactsOnly"
                 "enablePushNotificationsFromContactsOnly")]
    (fx/merge cofx
              (multiaccounts.update/optimistic :push-notifications-from-contacts-only? (boolean enabled?))
              {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) method)
                                 :params     []
                                 :on-success #(log/info "[push-notifications] contacts-notification-success" %)
                                 :on-error   #(re-frame/dispatch [::non-contacts-update-error enabled? %])}]})))
