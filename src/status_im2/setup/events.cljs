(ns status-im2.setup.events
  (:require [clojure.string :as string]
            [quo.theme :as quo.theme]
            [quo2.theme :as quo2.theme]
            [re-frame.core :as re-frame] ;; TODO (14/11/22 flexsurfer move to status-im2 namespace
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.native-module.core :as status]
            [status-im.utils.keychain.core :as keychain]
            [status-im2.common.theme.core :as theme]
            [status-im2.common.toasts.events]
            [status-im2.navigation.events :as navigation]
            [status-im2.setup.db :as db]
            [utils.re-frame :as rf]))

(re-frame/reg-fx
 :setup/open-multiaccounts
 (fn [callback]
   (status/open-accounts callback)))

(re-frame/reg-fx
 :setup/init-theme
 (fn []
   (theme/add-mode-change-listener #(re-frame/dispatch [:system-theme-mode-changed %]))
   (quo2.theme/set-theme (if (theme/dark-mode?) :dark :light))
   ;; TODO legacy support
   (quo.theme/set-theme (if (theme/dark-mode?) :dark :light))))

(rf/defn initialize-views
  {:events [:setup/initialize-view]}
  [cofx]
  (let [{{:multiaccounts/keys [multiaccounts]} :db} cofx]
    (if (and (seq multiaccounts))
      ;; We specifically pass a bunch of fields instead of the whole multiaccount
      ;; as we want store some fields in multiaccount that are not here
      (let [multiaccount (first (sort-by :timestamp > (vals multiaccounts)))]
        (rf/merge cofx
                  (multiaccounts.login/open-login (select-keys
                                                   multiaccount
                                                   [:key-uid :name :public-key :identicon :images]))
                  (keychain/get-auth-method (:key-uid multiaccount))))
      (navigation/init-root cofx :intro))))

(rf/defn initialize-multiaccounts
  {:events [:setup/initialize-multiaccounts]}
  [{:keys [db] :as cofx} all-multiaccounts {:keys [logout?]}]
  (let [multiaccounts (reduce (fn [acc
                                   {:keys [key-uid keycard-pairing]
                                    :as   multiaccount}]
                                (-> (assoc acc key-uid multiaccount)
                                    (assoc-in [key-uid :keycard-pairing]
                                              (when-not (string/blank? keycard-pairing)
                                                keycard-pairing))))
                              {}
                              all-multiaccounts)]
    (rf/merge cofx
              {:db         (-> db
                               (assoc :multiaccounts/multiaccounts multiaccounts)
                               (assoc :multiaccounts/logout? logout?)
                               (assoc :multiaccounts/loading false))
               :dispatch-n [[:setup/initialize-view]
                            [:get-opted-in-to-new-terms-of-service]
                            [:load-information-box-states]]})))

(rf/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys         [keycard supported-biometric-auth goto-key-storage?]
     :network/keys [type]
     :keycard/keys [banner-hidden]}
    :db}]
  {:db (assoc db/app-db
              :network/type             type
              :keycard/banner-hidden    banner-hidden
              :keycard                  (dissoc keycard :secrets :pin :application-info)
              :supported-biometric-auth supported-biometric-auth
              :goto-key-storage?        goto-key-storage?
              :multiaccounts/loading    true)})

(rf/defn start-app
  {:events [:setup/app-started]}
  [cofx]
  (rf/merge cofx
            {:setup/init-theme               nil
             :get-supported-biometric-auth   nil
             :network/listen-to-network-info nil
             :keycard/register-card-events   nil
             :keycard/check-nfc-support      nil
             :keycard/check-nfc-enabled      nil
             :keycard/retrieve-pairings      nil
             :setup/open-multiaccounts       #(do
                                                (re-frame/dispatch [:setup/initialize-multiaccounts %
                                                                    {:logout? false}])
                                                (re-frame/dispatch [:get-keycard-banner-preference]))}
            (initialize-app-db)))
