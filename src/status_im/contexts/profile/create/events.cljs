(ns status-im.contexts.profile.create.events
  (:require
    [native-module.core :as native-module]
    [status-im.contexts.profile.config :as profile.config]
    status-im.contexts.profile.create.effects
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx :profile.create/create-and-login
 (fn [{:keys [db]} [{:keys [display-name password image-path color]}]]
   (let [login-sha3-password (native-module/sha3 (security/safe-unmask-data password))]
     {:db (assoc-in db [:syncing :login-sha3-password] login-sha3-password)
      :fx [[:effects.profile/create-and-login
            (assoc (profile.config/create)
                   :displayName        display-name
                   :password           login-sha3-password
                   :imagePath          (profile.config/strip-file-prefix image-path)
                   :customizationColor color)]]})))
