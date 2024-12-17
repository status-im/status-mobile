(ns status-im.contexts.profile.recover.events
  (:require
    [native-module.core :as native-module]
    [status-im.constants :as constants]
    [status-im.contexts.profile.config :as profile.config]
    status-im.contexts.profile.recover.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx :profile.recover/recover-and-login
 (fn [{:keys [db]} [{:keys [display-name password image-path color seed-phrase]}]]
   (let [login-sha3-password (native-module/sha3 (security/safe-unmask-data password))]
     {:db
      (-> db
          (assoc :onboarding/recovered-account? true)
          (assoc-in [:syncing :login-sha3-password] login-sha3-password))
      :fx
      [[:effects.profile/restore-and-login
        (assoc (profile.config/create)
               :displayName        display-name
               :mnemonic           (security/safe-unmask-data seed-phrase)
               :password           login-sha3-password
               :imagePath          (profile.config/strip-file-prefix image-path)
               :customizationColor (or color constants/profile-default-color)
               :fetchBackup        true)]]})))

(rf/reg-event-fx :profile.recover/validate-recovery-phrase
 (fn [_ [phrase {:keys [on-success on-error]}]]
   {:effects.profile/validate-recovery-phrase [phrase on-success on-error]}))
