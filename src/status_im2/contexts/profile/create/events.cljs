(ns status-im2.contexts.profile.create.events
  (:require [utils.re-frame :as rf]
            [native-module.core :as native-module]
            [status-im2.contexts.profile.config :as profile.config]
            [status-im.ethereum.core :as ethereum]
            [utils.security.core :as security]
            [re-frame.core :as re-frame]))

(re-frame/reg-fx
 ::create-profile-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/create-account-and-login request)))

(rf/defn create-profile-and-login
  {:events [:profile.create/create-and-login]}
  [_ {:keys [display-name password image-path color]}]
  {::create-profile-and-login
   (merge (profile.config/create)
          {:displayName        display-name
           :password           (ethereum/sha3 (security/safe-unmask-data password))
           :imagePath          (profile.config/strip-file-prefix image-path)
           :customizationColor color})})
