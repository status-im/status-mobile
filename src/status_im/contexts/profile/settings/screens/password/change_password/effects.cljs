(ns status-im.contexts.profile.settings.screens.password.change-password.effects
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.common.keychain.events :as keychain]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-fx :effects.change-password/change-password
 (fn [{:keys [key-uid old-password new-password on-success on-fail]}]
   (let [hash-masked-password (fn [password]
                                (-> password security/hash-masked-password security/safe-unmask-data))
         old-password-hashed  (hash-masked-password old-password)
         new-password-hashed  (hash-masked-password new-password)]
     (-> (native-module/reset-password key-uid old-password-hashed new-password-hashed)
         (promesa/then #(keychain/save-user-password! key-uid new-password-hashed))
         (promesa/then on-success)
         (promesa/catch on-fail)))))
