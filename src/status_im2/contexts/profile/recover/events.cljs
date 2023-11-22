(ns status-im2.contexts.profile.recover.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im2.contexts.profile.config :as profile.config]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(re-frame/reg-fx
 ::restore-profile-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/restore-account-and-login request)))

(rf/defn recover-profile-and-login
  {:events [:profile.recover/recover-and-login]}
  [{:keys [db]} {:keys [display-name password image-path color seed-phrase]}]
  {:db
   (assoc db :onboarding-2/recovered-account? true)

   ::restore-profile-and-login
   (merge (profile.config/create)
          {:displayName        display-name
           :mnemonic           (security/safe-unmask-data seed-phrase)
           :password           (native-module/sha3 (security/safe-unmask-data password))
           :imagePath          (profile.config/strip-file-prefix image-path)
           :customizationColor color})})
