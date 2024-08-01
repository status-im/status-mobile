(ns status-im.contexts.profile.events
  (:require
    [legacy.status-im.data-store.settings :as data-store.settings]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [native-module.core :as native-module]
    [status-im.config :as config]
    [status-im.contexts.profile.edit.accent-colour.events]
    [status-im.contexts.profile.edit.bio.events]
    [status-im.contexts.profile.edit.header.events]
    [status-im.contexts.profile.edit.name.events]
    status-im.contexts.profile.login.events
    [status-im.contexts.profile.rpc :as profile.rpc]
    [utils.re-frame :as rf]))

(defn- select-profile
  [profile key-uid]
  (-> profile
      (assoc :key-uid key-uid)
      (dissoc :error :password)))

(defn- reduce-profiles
  [profiles]
  (reduce
   (fn [acc {:keys [key-uid] :as profile}]
     (assoc acc key-uid (profile.rpc/rpc->profiles-overview profile)))
   {}
   profiles))

(rf/reg-fx
 :profile/get-profiles-overview
 (fn [callback]
   (native-module/initialize-application {:dataDir       (native-module/backup-disabled-data-dir)
                                          :mixpanelAppId config/mixpanel-app-id
                                          :mixpanelToken config/mixpanel-token}
                                         callback)))

(rf/reg-event-fx
 :profile/profile-selected
 (fn [{:keys [db]} [key-uid]]
   {:db (update db :profile/login #(select-profile % key-uid))}))

(rf/reg-event-fx
 :profile/get-profiles-overview-success
 (fn [{:keys [db]} [{:keys [accounts] {:keys [userConfirmed enabled]} :centralizedMetricsInfo}]]
   (let [db-with-settings (assoc db
                                 :centralized-metrics/user-confirmed? userConfirmed
                                 :centralized-metrics/enabled?        enabled)]
     (if (seq accounts)
       (let [profiles          (reduce-profiles accounts)
             {:keys [key-uid]} (first (sort-by :timestamp > (vals profiles)))]
         {:db (if key-uid
                (-> db-with-settings
                    (assoc :profile/profiles-overview profiles)
                    (update :profile/login #(select-profile % key-uid)))
                db-with-settings)
          :fx [[:dispatch [:init-root :screen/profile.profiles]]
               (when (and key-uid userConfirmed)
                 [:effects.biometric/check-if-available
                  {:key-uid    key-uid
                   :on-success (fn [auth-method]
                                 (rf/dispatch [:profile.login/check-biometric-success key-uid
                                               auth-method]))}])]})
       {:db db-with-settings
        :fx [[:dispatch [:init-root :screen/onboarding.intro]]]}))))

(rf/reg-event-fx
 :profile/update-setting-from-backup
 (fn [{:keys [db]} [{:keys [backedUpSettings]}]]
   (let [setting              (update backedUpSettings :name keyword)
         {:keys [name value]} (data-store.settings/rpc->setting-value setting)]
     {:db (assoc-in db [:profile/profile name] value)})))

(rf/reg-event-fx
 :profile/update-profile-from-backup
 (fn [_ [{{:keys [ensUsernameDetails]} :backedUpProfile}]]
   {:fx [[:dispatch [:ens/update-usernames ensUsernameDetails]]]}))

(rf/reg-event-fx :profile/update-messages-from-contacts-only
 (fn [{:keys [db] :as cofx}]
   (multiaccounts.update/multiaccount-update
    cofx
    :messages-from-contacts-only
    (not (get-in db [:profile/profile :messages-from-contacts-only]))
    {})))
