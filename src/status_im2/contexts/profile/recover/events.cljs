(ns status-im2.contexts.profile.recover.events
  (:require [utils.security.core :as security]
            [status-im.ethereum.core :as ethereum]
            [status-im2.contexts.profile.config :as profile.config]
            [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [native-module.core :as native-module]))

(re-frame/reg-fx
 ::restore-profile-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/restore-account-and-login request)))

(rf/defn recover-profile-and-login
  {:events [:profile.recover/recover-and-login]}
  [_ {:keys [display-name password image-path color seed-phrase]}]
  {::restore-profile-and-login
   (merge (profile.config/create)
          {:displayName        display-name
           :mnemonic           (security/safe-unmask-data seed-phrase)
           :password           (ethereum/sha3 (security/safe-unmask-data password))
           :imagePath          (profile.config/strip-file-prefix image-path)
           :customizationColor color})})
