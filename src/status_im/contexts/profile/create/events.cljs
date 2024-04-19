(ns status-im.contexts.profile.create.events
  (:require
    status-im.contexts.profile.create.effects
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :profile.create/create-and-login
 (fn [{:keys [db]} [profile]]
   (let [{:keys [password] :as profile} (profile.utils/create-profile-config profile false)]
     {:db                               (assoc-in db [:syncing :login-sha3-password] password)
      :effects.profile/create-and-login profile})))
