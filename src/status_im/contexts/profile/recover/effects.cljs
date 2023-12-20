(ns status-im.contexts.profile.recover.effects
  (:require
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.profile/restore-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/restore-account-and-login request)))
