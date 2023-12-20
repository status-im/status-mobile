(ns status-im.contexts.profile.create.effects
  (:require
    [native-module.core :as native-module]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.profile/create-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/create-account-and-login request)))
