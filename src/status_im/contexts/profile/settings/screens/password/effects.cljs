(ns status-im.contexts.profile.settings.screens.password.effects
  (:require [native-module.core :as native-module]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-fx
 :effects.password-settings/change-password
 (fn [{:keys [key-uid old-password new-password on-success]}]
   (native-module/reset-password
    key-uid
    (native-module/sha3 (security/safe-unmask-data old-password))
    (native-module/sha3 (security/safe-unmask-data new-password))
    on-success)))
