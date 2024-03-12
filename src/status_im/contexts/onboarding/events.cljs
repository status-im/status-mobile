(ns status-im.contexts.onboarding.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    status-im.common.biometric.events
    [status-im.constants :as constants]
    [status-im.contexts.profile.create.events :as profile.create]
    [status-im.contexts.profile.recover.events :as profile.recover]
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

(rf/defn profile-data-set
  {:events [:onboarding/profile-data-set]}
  [{:keys [db]} onboarding-data]
  {:db       (update db :onboarding/profile merge onboarding-data)
   :dispatch [:navigate-to-within-stack [:create-profile-password :new-to-status]]})

(rf/defn enable-biometrics
  {:events [:onboarding/enable-biometrics]}
  [_]
  {:biometric/authenticate {:on-success #(rf/dispatch [:onboarding/biometrics-done])
                            :on-fail    #(rf/dispatch [:onboarding/biometrics-fail %])}})

(rf/defn navigate-to-enable-notifications
  {:events [:onboarding/navigate-to-enable-notifications]}
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db       (dissoc db :onboarding/profile)
     :dispatch [:navigate-to-within-stack [:enable-notifications :enable-biometrics]]}))

(rf/defn biometrics-done
  {:events [:onboarding/biometrics-done]}
  [{:keys [db]}]
  (let [syncing? (get-in db [:onboarding/profile :syncing?])]
    {:db       (assoc-in db [:onboarding/profile :auth-method] constants/auth-method-biometric)
     :dispatch (if syncing?
                 [:onboarding/finalize-setup]
                 [:onboarding/create-account-and-login])}))

(rf/reg-event-fx
 :onboarding/biometrics-fail
 (fn [_ [error]]
   {:dispatch [:biometric/show-message (ex-cause error)]}))

(rf/defn create-account-and-login
  {:events [:onboarding/create-account-and-login]}
  [{:keys [db] :as cofx}]
  (let [{:keys [display-name seed-phrase password image-path color] :as profile}
        (:onboarding/profile db)]
    (rf/merge cofx
              {:dispatch       [:navigate-to-within-stack [:generating-keys :new-to-status]]
               :dispatch-later [{:ms       constants/onboarding-generating-keys-animation-duration-ms
                                 :dispatch [:onboarding/navigate-to-identifiers]}]
               :db             (-> db
                                   (dissoc :profile/login)
                                   (dissoc :auth-method)
                                   (assoc :onboarding/new-account? true))}
              (if seed-phrase
                (profile.recover/recover-profile-and-login profile)
                (profile.create/create-profile-and-login profile)))))

(rf/defn on-delete-profile-success
  {:events [:onboarding/on-delete-profile-success]}
  [{:keys [db]} key-uid]
  (let [multiaccounts (dissoc (:profile/profiles-overview db) key-uid)]
    (merge
     {:db (assoc db :profile/profiles-overview multiaccounts)}
     (when-not (seq multiaccounts)
       {:set-root :intro}))))

(rf/defn password-set
  {:events [:onboarding/password-set]}
  [{:keys [db]} password]
  (let [supported-type (:biometric/supported-type db)]
    {:db       (-> db
                   (assoc-in [:onboarding/profile :password] password)
                   (assoc-in [:onboarding/profile :auth-method] constants/auth-method-password))
     :dispatch (if supported-type
                 [:navigate-to-within-stack [:enable-biometrics :new-to-status]]
                 [:onboarding/create-account-and-login])}))

(rf/defn navigate-to-enable-biometrics
  {:events [:onboarding/navigate-to-enable-biometrics]}
  [{:keys [db]}]
  (let [supported-type (:biometric/supported-type db)]
    {:dispatch (if supported-type
                 [:open-modal :enable-biometrics]
                 [:open-modal :enable-notifications])}))

(rf/defn seed-phrase-entered
  {:events [:onboarding/seed-phrase-entered]}
  [_ seed-phrase on-error]
  {:multiaccount/validate-mnemonic [seed-phrase
                                    (fn [mnemonic key-uid]
                                      (re-frame/dispatch [:onboarding/seed-phrase-validated
                                                          mnemonic key-uid]))
                                    on-error]})

(rf/defn seed-phrase-validated
  {:events [:onboarding/seed-phrase-validated]}
  [{:keys [db]} seed-phrase key-uid]
  (if (contains? (:profile/profiles-overview db) key-uid)
    {:effects.utils/show-confirmation
     {:title               (i18n/label :t/multiaccount-exists-title)
      :content             (i18n/label :t/multiaccount-exists-content)
      :confirm-button-text (i18n/label :t/unlock)
      :on-accept           (fn []
                             (re-frame/dispatch [:pop-to-root :profiles])
                             (re-frame/dispatch
                              [:profile/profile-selected key-uid]))
      :on-cancel           #(re-frame/dispatch [:pop-to-root :multiaccounts])}}
    {:db       (assoc-in db [:onboarding/profile :seed-phrase] seed-phrase)
     :dispatch [:navigate-to-within-stack [:create-profile :new-to-status]]}))

(rf/defn navigate-to-create-profile
  {:events [:onboarding/navigate-to-create-profile]}
  [{:keys [db]}]
  ;; Restart the flow
  {:db       (dissoc db :onboarding/profile)
   :dispatch [:navigate-to-within-stack [:create-profile :new-to-status]]})

(rf/reg-event-fx :onboarding/set-auth-method
 (fn [{:keys [db]} [auth-method]]
   {:db (assoc db :auth-method auth-method)}))

(rf/defn onboarding-new-account-finalize-setup
  {:events [:onboarding/finalize-setup]}
  [{:keys [db]}]
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
                                            :error   %})}))))

(rf/defn navigate-to-identifiers
  {:events [:onboarding/navigate-to-identifiers]}
  [{:keys [db]}]
  (if (:onboarding/generated-keys? db)
    {:dispatch [:navigate-to-within-stack [:identifiers :new-to-status]]}
    {:dispatch-later [{:ms       constants/onboarding-generating-keys-navigation-retry-ms
                       :dispatch [:onboarding/navigate-to-identifiers]}]}))
