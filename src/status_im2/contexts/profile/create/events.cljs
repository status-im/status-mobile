(ns status-im2.contexts.profile.create.events
  (:require
    [native-module.core :as native-module]
    [status-im2.contexts.emoji-picker.utils :as emoji-picker.utils]
    [status-im2.contexts.profile.config :as profile.config]
    status-im2.contexts.profile.create.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn create-profile-and-login
  {:events [:profile.create/create-and-login]}
  [{:keys [db]} {:keys [display-name password image-path color]}]
  (let [login-sha3-password (native-module/sha3 (security/safe-unmask-data password))]
    {:db (assoc-in db [:syncing :login-sha3-password] login-sha3-password)
     :effects.profile/create-and-login
     (assoc (profile.config/create)
            :displayName        display-name
            :password           login-sha3-password
            :imagePath          (profile.config/strip-file-prefix image-path)
            :customizationColor color
            :emoji              (emoji-picker.utils/random-emoji))}))
