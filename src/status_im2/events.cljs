(ns status-im2.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [native-module.core :as native-module]
            [status-im.utils.keychain.core :as keychain]
            [status-im2.common.json-rpc.events]
            [status-im2.common.theme.core :as theme]
            [status-im2.common.toasts.events]
            [status-im2.contexts.add-new-contact.events]
            status-im2.contexts.onboarding.events
            [status-im.bottom-sheet.events]
            [status-im2.navigation.events :as navigation]
            [status-im2.db :as db]
            [utils.re-frame :as rf]
            [utils.datetime :as datetime]
            status-im2.contexts.share.events
            status-im2.contexts.syncing.events
            status-im2.contexts.chat.events))

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (datetime/timestamp))))

(re-frame/reg-fx
 :setup/open-multiaccounts
 (fn [callback]
   (native-module/open-accounts callback)))

(re-frame/reg-fx
 :setup/init-theme
 (fn []
   (theme/add-device-theme-change-listener)))

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
                                                   [:key-uid :name :public-key :images
                                                    :customization-color]))
                  (keychain/get-auth-method (:key-uid multiaccount))))
      (navigation/init-root cofx :intro))))

(defn rpc->multiaccount
  [{:keys [customizationColor keycard-pairing] :as multiaccount}]
  (-> multiaccount
      (dissoc :customizationColor)
      (assoc :customization-color (keyword customizationColor))
      (assoc :keycard-pairing
             (when-not
               (string/blank? keycard-pairing)
               keycard-pairing))))

(rf/defn initialize-multiaccounts
  {:events [:setup/initialize-multiaccounts]}
  [{:keys [db] :as cofx} all-multiaccounts {:keys [logout?]}]
  (let [multiaccounts (reduce (fn [acc
                                   {:keys [key-uid keycard-pairing]
                                    :as   multiaccount}]
                                (assoc acc key-uid (rpc->multiaccount multiaccount)))
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
