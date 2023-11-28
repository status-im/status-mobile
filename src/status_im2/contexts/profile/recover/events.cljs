(ns status-im2.contexts.profile.recover.events
  (:require
    [native-module.core :as native-module]
<<<<<<< HEAD
=======
    [re-frame.core :as re-frame]
    [status-im2.contexts.emoji-picker.utils :as emoji-picker.utils]
>>>>>>> d46724bcf (qa)
    [status-im2.contexts.profile.config :as profile.config]
    status-im2.contexts.profile.recover.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn recover-profile-and-login
<<<<<<< HEAD
  {:events [:profile.recover/recover-and-login]}
  [{:keys [db]} {:keys [display-name password image-path color seed-phrase]}]
  {:db
   (assoc db :onboarding/recovered-account? true)

   :effects.profile/restore-and-login
   (merge (profile.config/create)
          {:displayName        display-name
           :mnemonic           (security/safe-unmask-data seed-phrase)
           :password           (native-module/sha3 (security/safe-unmask-data password))
           :imagePath          (profile.config/strip-file-prefix image-path)
           :customizationColor color})})
=======
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
                  :customizationColor color
                  :emoji              (emoji-picker.utils/random-emoji)})})
>>>>>>> d46724bcf (qa)
