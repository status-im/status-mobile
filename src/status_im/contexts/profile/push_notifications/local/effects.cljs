(ns status-im.contexts.profile.push-notifications.local.effects
  (:require
    [cljs-bean.core :as bean]
    [native-module.push-notifications :as native-module.pn]
    [react-native.push-notification-ios :as pn-ios]
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
