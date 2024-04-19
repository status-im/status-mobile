(ns status-im.contexts.onboarding.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    status-im.common.biometric.events
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(re-frame/reg-fx
 :multiaccount/validate-mnemonic
 (fn [[mnemonic on-success on-error]]
   (native-module/validate-mnemonic
    (security/safe-unmask-data mnemonic)
    (fn [result]
      (let [{:keys [error keyUID]} (transforms/json->clj result)]
        (if (seq error)
          (when on-error (on-error error))
          (on-success mnemonic keyUID)))))))

(rf/reg-event-fx
 :onboarding/profile-data-set
 (fn [{:keys [db]} [onboarding-data]]
   {:db (update db :onboarding/profile merge onboarding-data)
    :fx [[:dispatch
          [:navigate-to-within-stack
           [:screen/onboarding.create-profile-password :screen/onboarding.new-to-status]]]]}))

(rf/reg-event-fx
 :onboarding/navigate-to-enable-notifications
 (fn [{:keys [db]}]
   {:db (dissoc db :onboarding/profile)
    :fx [[:dispatch
          [:navigate-to-within-stack
           [:screen/onboarding.enable-notifications :screen/onboarding.enable-biometrics]]]]}))

(rf/reg-event-fx
 :onboarding/biometrics-done
 (fn [{:keys [db]}]
   (let [syncing? (get-in db [:onboarding/profile :syncing?])]
     {:db (assoc-in db [:onboarding/profile :auth-method] constants/auth-method-biometric)
      :fx [[:dispatch
            (if syncing?
              [:onboarding/finalize-setup]
              [:onboarding/create-account-and-login])]]})))

(rf/reg-event-fx
 :onboarding/create-account-and-login
 (fn [{:keys [db]}]
   (let [{:keys [seed-phrase] :as profile} (:onboarding/profile db)
         restore?                          (boolean seed-phrase)
         {:keys [password] :as profile}    (profile.utils/create-profile-config profile restore?)]
     (cond-> {:fx [[:dispatch
                    [:navigate-to-within-stack
                     [:screen/onboarding.generating-keys :screen/onboarding.new-to-status]]]
                   [:dispatch-later
                    [{:ms       constants/onboarding-generating-keys-animation-duration-ms
                      :dispatch [:onboarding/navigate-to-identifiers]}]]]
              :db (-> db
                      (dissoc :profile/login)
                      (dissoc :auth-method)
                      (assoc :onboarding/new-account? true)
                      (assoc-in [:syncing :login-sha3-password] password))}

       restore?
       (assoc-in [:db :onboarding/recovered-account?] true)

       restore?
       (assoc :effects.profile/restore-and-login profile)

       (not restore?)
       (assoc :effects.profile/create-and-login profile)))))

(rf/reg-event-fx
 :onboarding/on-delete-profile-success
 (fn [{:keys [db]} [key-uid]]
   (let [multiaccounts (dissoc (:profile/profiles-overview db) key-uid)]
     (merge
      {:db (assoc db :profile/profiles-overview multiaccounts)}
      (when-not (seq multiaccounts)
        {:set-root :screen/onboarding.intro})))))

(rf/reg-event-fx
 :onboarding/password-set
 (fn [{:keys [db]} [password]]
   (let [supported-type (get-in db [:biometrics :supported-type])]
     {:db (-> db
              (assoc-in [:onboarding/profile :password] password)
              (assoc-in [:onboarding/profile :auth-method] constants/auth-method-password))
      :fx [[:dispatch
            (if supported-type
              [:navigate-to-within-stack
               [:screen/onboarding.enable-biometrics :screen/onboarding.new-to-status]]
              [:onboarding/create-account-and-login])]]})))

(rf/reg-event-fx
 :onboarding/navigate-to-enable-biometrics
 (fn [{:keys [db]}]
   (let [supported-type (get-in db [:biometrics :supported-type])]
     {:fx [[:dispatch
            (if supported-type
              [:open-modal :screen/onboarding.enable-biometrics]
              [:open-modal :screen/onboarding.enable-notifications])]]})))

(rf/reg-event-fx
 :onboarding/seed-phrase-entered
 (fn [_ [seed-phrase on-error]]
   {:multiaccount/validate-mnemonic [seed-phrase
                                     (fn [mnemonic key-uid]
                                       (rf/dispatch [:onboarding/seed-phrase-validated
                                                     mnemonic key-uid]))
                                     on-error]}))

(rf/reg-event-fx
 :onboarding/seed-phrase-validated
 (fn [{:keys [db]} [seed-phrase key-uid]]
   (if (contains? (:profile/profiles-overview db) key-uid)
     {:effects.utils/show-confirmation
      {:title               (i18n/label :t/multiaccount-exists-title)
       :content             (i18n/label :t/multiaccount-exists-content)
       :confirm-button-text (i18n/label :t/unlock)
       :on-accept           (fn []
                              (re-frame/dispatch [:pop-to-root :screen/profile.profiles])
                              (re-frame/dispatch
                               [:profile/profile-selected key-uid]))
       :on-cancel           #(re-frame/dispatch [:pop-to-root :multiaccounts])}}
     {:db (assoc-in db [:onboarding/profile :seed-phrase] seed-phrase)
      :fx [[:dispatch
            [:navigate-to-within-stack
             [:screen/onboarding.create-profile :screen/onboarding.new-to-status]]]]})))

(rf/reg-event-fx
 :onboarding/navigate-to-create-profile
 (fn [{:keys [db]}]
   {:db (dissoc db :onboarding/profile)
    :fx [[:dispatch
          [:navigate-to-within-stack
           [:screen/onboarding.create-profile :screen/onboarding.new-to-status]]]]}))

(rf/reg-event-fx
 :onboarding/set-auth-method
 (fn [{:keys [db]} [auth-method]]
   {:db (assoc db :auth-method auth-method)}))

(rf/reg-event-fx
 :onboarding/finalize-setup
 (fn [{:keys [db]}]
   (let [masked-password    (get-in db [:onboarding/profile :password])
         key-uid            (get-in db [:profile/profile :key-uid])
         syncing?           (get-in db [:onboarding/profile :syncing?])
         auth-method        (get-in db [:onboarding/profile :auth-method])
         biometric-enabled? (= auth-method
                               constants/auth-method-biometric)]
     (cond-> {:db (assoc db :onboarding/generated-keys? true)}
       biometric-enabled?
       (assoc :keychain/save-password-and-auth-method
              {:key-uid         key-uid
               :masked-password (if syncing?
                                  masked-password
                                  (security/hash-masked-password masked-password))
               :on-success      (fn []
                                  (rf/dispatch [:onboarding/set-auth-method auth-method])
                                  (when syncing?
                                    (rf/dispatch [:onboarding/navigate-to-enable-notifications])))
               :on-error        #(log/error "failed to save biometrics"
                                            {:key-uid key-uid
                                             :error   %})})))))

(rf/reg-event-fx
 :onboarding/navigate-to-identifiers
 (fn [{:keys [db]}]
   {:fx [(if (:onboarding/generated-keys? db)
           [:dispatch
            [:navigate-to-within-stack
             [:screen/onboarding.identifiers :screen/onboarding.new-to-status]]]
           [:dispatch-later
            [{:ms       constants/onboarding-generating-keys-navigation-retry-ms
              :dispatch [:onboarding/navigate-to-identifiers]}]])]}))
