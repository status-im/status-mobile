(ns status-im.contexts.profile.recover.events
  (:require
    status-im.contexts.profile.recover.effects
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :profile.create/recover-and-login
 (fn [{:keys [db]} [profile]]
   (let [{:keys [password] :as profile} (profile.utils/create-profile-config profile true)]
     {:db                               (-> db
                                            (assoc :onboarding/recovered-account? true)
                                            (assoc-in [:syncing :login-sha3-password] password))
      :effects.profile/create-and-login profile})))
