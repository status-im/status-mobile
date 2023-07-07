(ns status-im2.contexts.profile.events
  (:require [utils.re-frame :as rf]
            [status-im2.contexts.profile.rpc :as profile.rpc]
            [re-frame.core :as re-frame]
            [native-module.core :as native-module]
            [status-im2.navigation.events :as navigation]
            [status-im2.contexts.profile.login.events :as login]))

(re-frame/reg-fx
 :profile/get-profiles-overview
 (fn [callback]
   (native-module/open-accounts callback)))

(rf/defn profile-selected
  {:events [:profile/profile-selected]}
  [{:keys [db]} key-uid]
  {:db (update db
               :profile/login
               #(-> %
                    (assoc :key-uid key-uid)
                    (dissoc :error :password)))})

(rf/defn init-profiles-overview
  [{:keys [db] :as cofx} profiles key-uid]
  (rf/merge cofx
            {:db (assoc db :profile/profiles-overview profiles)}
            (profile-selected key-uid)))

(defn reduce-profiles
  [profiles]
  (reduce
   (fn [acc {:keys [key-uid] :as profile}]
     (assoc acc key-uid (profile.rpc/rpc->profiles-overview profile)))
   {}
   profiles))

(rf/defn get-profiles-overview-success
  {:events [:profile/get-profiles-overview-success]}
  [cofx profiles-overview]
  (if (seq profiles-overview)
    (let [profiles          (reduce-profiles profiles-overview)
          {:keys [key-uid]} (first (sort-by :timestamp > (vals profiles)))]
      (rf/merge cofx
                (navigation/init-root :profiles)
                (init-profiles-overview profiles key-uid)
                ;;we check if biometric is available, and try to login with it,
                ;;if succeed "node.login" signal will be triggered
                (login/login-with-biometric-if-available key-uid)))
    (navigation/init-root cofx :intro)))
