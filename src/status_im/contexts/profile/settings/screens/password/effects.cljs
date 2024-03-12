(ns status-im.contexts.profile.settings.screens.password.effects
  (:require [native-module.core :as native-module]
            [promesa.core :as p]
            [status-im.common.keychain.events :as keychain]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-fx
 :effects.password-settings/change-password
 (fn [{:keys [key-uid old-password new-password on-success on-fail]}]
   (let [hash-masked-password (fn [pw]
                                (-> pw security/safe-unmask-data native-module/sha3))
         old-password-hashed  (hash-masked-password old-password)
         new-password-hashed  (hash-masked-password new-password)]
     (-> (native-module/reset-password key-uid old-password-hashed new-password-hashed)
         (p/then #(keychain/save-user-password! key-uid new-password-hashed))
         (p/then on-success)
         (p/catch on-fail)))))
