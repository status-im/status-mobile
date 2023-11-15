(ns status-im2.contexts.profile.create.events
  (:require
    [native-module.core :as native-module]
    [status-im2.contexts.profile.config :as profile.config]
    status-im2.contexts.profile.create.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn create-profile-and-login
  {:events [:profile.create/create-and-login]}
<<<<<<< HEAD
  [_ {:keys [display-name password image-path color]}]
  {:effects.profile/create-and-login
=======
  [_ {:keys [display-name password image-path color emoji]}]
  {::create-profile-and-login
>>>>>>> c1d06ad1d (wallet: add color and emoji)
   (assoc (profile.config/create)
          :displayName        display-name
          :password           (native-module/sha3 (security/safe-unmask-data password))
          :imagePath          (profile.config/strip-file-prefix image-path)
          :customizationColor color
          :emoji              emoji)})
