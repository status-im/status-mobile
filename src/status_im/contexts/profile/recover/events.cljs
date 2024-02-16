(ns status-im.contexts.profile.recover.events
  (:require
    [native-module.core :as native-module]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.contexts.profile.config :as profile.config]
    status-im.contexts.profile.recover.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn recover-profile-and-login
  {:events [:profile.recover/recover-and-login]}
  [{:keys [db]} {:keys [display-name password image-path color seed-phrase]}]
  (let [login-sha3-password (native-module/sha3 (security/safe-unmask-data password))]
    {:db
     (-> db
         (assoc :onboarding/recovered-account? true)
         (assoc-in [:syncing :login-sha3-password] login-sha3-password))

     :effects.profile/restore-and-login
     (merge (profile.config/create)
            {:displayName        display-name
             :mnemonic           (security/safe-unmask-data seed-phrase)
             :password           login-sha3-password
             :imagePath          (profile.config/strip-file-prefix image-path)
             :customizationColor color
             :emoji              (emoji-picker.utils/random-emoji)})}))
