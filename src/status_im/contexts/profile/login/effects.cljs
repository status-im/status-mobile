(ns status-im.contexts.profile.login.effects
  (:require
    [native-module.core :as native-module]
    [status-im.contexts.profile.config :as profile.config]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.profile/login
 (fn [[key-uid hashed-password]]
   ;;"node.login" signal will be triggered as a callback
   (native-module/login-account
    (assoc (profile.config/login) :keyUid key-uid :password hashed-password))))

(rf/reg-fx :effects.profile/enable-local-notifications
 (fn []
   (native-module/start-local-notifications)))

(rf/reg-fx :effects.profile/convert-to-keycard-profile
 (fn [{:keys [profile settings password new-password callback]}]
   (native-module/convert-to-keycard-profile profile settings password new-password callback)))
