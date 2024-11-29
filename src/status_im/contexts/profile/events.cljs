(ns status-im.contexts.profile.events
  (:require
    [clojure.string :as string]
    [legacy.status-im.data-store.settings :as data-store.settings]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [native-module.core :as native-module]
    [status-im.config :as config]
    [status-im.contexts.profile.data-store :as profile.data-store]
    [status-im.contexts.profile.edit.accent-colour.events]
    [status-im.contexts.profile.edit.bio.events]
    [status-im.contexts.profile.edit.header.events]
    [status-im.contexts.profile.edit.name.events]
    status-im.contexts.profile.effects
    status-im.contexts.profile.login.events
    status-im.contexts.profile.logout.events
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
   (native-module/initialize-application
    {:dataDir              (native-module/backup-disabled-data-dir)
     :mixpanelAppId        config/mixpanel-app-id
     :mixpanelToken        config/mixpanel-token
     :mediaServerEnableTLS (config/enabled? config/STATUS_BACKEND_SERVER_MEDIA_SERVER_ENABLE_TLS)
     :logEnabled           (not (string/blank? config/log-level))
     :logLevel             config/log-level
     :apiLoggingEnabled    config/api-logging-enabled?}
    callback)))

(rf/reg-event-fx
 :profile/profile-selected
 (fn [{:keys [db]} [key-uid]]
   {:db (update db :profile/login select-profile key-uid)}))

(rf/reg-event-fx
 :profile/set-profile-overview-auth-method
 (fn [{db :db} [key-uid auth-method]]
   {:db (assoc-in db [:profile/profiles-overview key-uid :auth-method] auth-method)}))

(rf/reg-event-fx
 :profile/get-profiles-auth-method
 (fn [_ [key-uids]]
   (let [auth-method-fx (fn [key-uid]
                          [:effects.biometric/check-if-available
                           {:key-uid    key-uid
                            :on-success (fn [auth-method]
                                          (rf/dispatch
                                           [:profile/set-profile-overview-auth-method
                                            key-uid
                                            auth-method]))}])]
     {:fx (map auth-method-fx key-uids)})))

(rf/reg-event-fx
 :profile/set-already-logged-out
 (fn [{:keys [db]}]
   {:db (dissoc db :profile/logging-out?)}))

(rf/reg-event-fx
 :profile/get-profiles-overview-success
 (fn [{:keys [db]}
      [{accounts                        :accounts
        {:keys [userConfirmed enabled]} :centralizedMetricsInfo}]]
   (let [profiles          (reduce-profiles accounts)
         profiles-key-uids (keys profiles)
         new-db            (cond-> db
                             :always
                             (assoc :centralized-metrics/user-confirmed? userConfirmed
                                    :centralized-metrics/enabled?        enabled)

                             (seq profiles)
                             (assoc :profile/profiles-overview profiles))]
     {:db new-db
      :fx [[:dispatch [:profile/get-profiles-auth-method profiles-key-uids]]
           (if (profile.data-store/accepted-terms? accounts)
             [:dispatch [:update-theme-and-init-root :screen/profile.profiles]]
             [:dispatch [:update-theme-and-init-root :screen/onboarding.intro]])
           ;; dispatch-later makes sure that the logout button subscribed is always disabled
           [:dispatch-later
            {:ms       100
             :dispatch [:profile/set-already-logged-out]}]]})))

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

(rf/reg-event-fx :profile/explore-new-status
 (fn []
   {:fx [[:effects.profile/accept-terms
          {:on-success [:navigate-to :screen/profile.profiles]}]]}))
