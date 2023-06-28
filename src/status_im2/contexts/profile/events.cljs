(ns status-im2.contexts.profile.events
  (:require [utils.re-frame :as rf]
            [clojure.string :as string]))

(rf/defn profile-selected
  {:events [:profile/profile-selected]}
  [{:keys [db]} key-uid]
  {:db (update db
               :profile/login
               #(-> %
                    (assoc :key-uid key-uid)
                    (dissoc :error :password)))})

(defn rpc->profiles-overview
  [{:keys [customizationColor keycard-pairing] :as profile}]
  (-> profile
      (dissoc :customizationColor)
      (assoc :customization-color (keyword customizationColor))
      (assoc :keycard-pairing (when-not (string/blank? keycard-pairing) keycard-pairing))))

(rf/defn init-profiles-overview
  [{:keys [db] :as cofx} profiles]
  (let [profiles          (reduce
                           (fn [acc {:keys [key-uid] :as profile}]
                             (assoc acc key-uid (rpc->profiles-overview profile)))
                           {}
                           profiles)
        {:keys [key-uid]} (first (sort-by :timestamp > (vals profiles)))]
    (rf/merge cofx
              {:db (assoc db :profile/profiles-overview profiles)
               :keychain/get-auth-method
               [key-uid #(rf/dispatch [:multiaccounts.login/get-auth-method-success % key-uid])]
               :dispatch-n [[:get-opted-in-to-new-terms-of-service]
                            [:load-information-box-states]]}
              (profile-selected key-uid))))
