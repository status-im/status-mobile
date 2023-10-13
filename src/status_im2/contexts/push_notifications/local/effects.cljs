(ns status-im2.contexts.push-notifications.local.effects
  (:require [react-native.push-notification-ios :as pn-ios]
            [cljs-bean.core :as bean]
            [native-module.push-notifications :as native-module.pn]
            [utils.re-frame :as rf]))

(rf/reg-fx :effects/push-notifications-local-present-ios
           (fn [{:keys [title message user-info body-type]}]
             (when (not= body-type "message")
               (pn-ios/present-local-notification title
                                                  message
                                                  (bean/->js (merge user-info
                                                                    {:notificationType
                                                                     "local-notification"}))))))

(rf/reg-fx :effects/push-notifications-local-present-android
           (fn [notification]
             (native-module.pn/present-local-notification notification)))
