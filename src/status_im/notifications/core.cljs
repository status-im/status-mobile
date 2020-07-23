(ns status-im.notifications.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            ["react-native-push-notification" :as rn-pn]
            [quo.platform :as platform]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.utils.handlers :as handlers]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [status-im.ui.components.react :as react]))

(defn enable-ios-notifications []
  (.configure
   ^js rn-pn
   (clj->js {:onRegister (fn [token-data]
                           ;;TODO register token in status pn node send waku message
                           (let [token (.-token ^js token-data)]
                             (utils/show-popup nil
                                               (str "Token " token)
                                               #())
                             (react/copy-to-clipboard token)
                             (println "TOKEN " token)))})))

(defn disable-ios-notifications []
  ;;TODO remove token from status pn node, send waku message
  (.abandonPermissions ^js rn-pn))

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

(handlers/register-handler-fx
 ::registered-for-push-notifications
 (fn [cofx]))

(handlers/register-handler-fx
 ::unregistered-from-push-notifications
 (fn [cofx]))


(fx/defn handle-enable-notifications-event [cofx]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "registerForPushNotifications")
                     :params ["token"]
                     :on-success #(re-frame/dispatch [::registered-for-push-notifications %])}]})

(fx/defn handle-disable-notifications-event [cofx]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "unregisterForPushNotifications")
                     :params []
                     :on-success #(re-frame/dispatch [::unregistered-from-push-notifications %])}]})
