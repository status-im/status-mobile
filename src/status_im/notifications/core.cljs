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
                               (re-frame/dispatch [::enable-error error]))}))

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
                     :on-success #(log/info "[push-notifications] register-success" %)}]})

(fx/defn handle-disable-notifications-event
  {:events [::unregistered-from-push-notifications]}
  [cofx]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "unregisterForPushNotifications")
                     :params     []
                     :on-success #(log/info "[push-notifications] unregister-success" %)}]})

(fx/defn enable-error
  {:events [::enable-error]}
  [{:keys [db] :as cofx} _]
  ;; NOTE(Ferossgp): Should we alert user about error?
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update :notifications-enabled? false {})))

(fx/defn notification-switch
  {:events [::switch]}
  [{:keys [db] :as cofx} enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :notifications-enabled? (boolean enabled?) {})
            (if enabled?
              {::enable nil}
              {::disable nil})))

(fx/defn notification-non-contacts
  {:events [::switch-non-contacts]}
  [{:keys [db] :as cofx} enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :notifications-non-contact? (boolean enabled?) {})))
