(ns status-im2.contexts.onboarding.events
  (:require [native-module.core :as native-module]
            [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [status-im2.constants :as constants]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [status-im2.contexts.profile.create.events :as profile.create]
            [status-im2.contexts.profile.recover.events :as profile.recover]
            [status-im2.common.biometric.events :as biometric]))

(re-frame/reg-fx
 :multiaccount/validate-mnemonic
 (fn [[mnemonic on-success on-error]]
   (native-module/validate-mnemonic
    (security/safe-unmask-data mnemonic)
    (fn [result]
      (let [{:keys [error keyUID]} (types/json->clj result)]
        (if (seq error)
          (when on-error (on-error error))
          (on-success mnemonic keyUID)))))))

(rf/defn profile-data-set
  {:events [:onboarding-2/profile-data-set]}
  [{:keys [db]} onboarding-data]
  {:db       (update db :onboarding-2/profile merge onboarding-data)
   :dispatch [:navigate-to :create-profile-password]})

(rf/defn enable-biometrics
  {:events [:onboarding-2/enable-biometrics]}
  [_]
  {:biometric/authenticate {:on-success #(rf/dispatch [:onboarding-2/biometrics-done])
                            :on-fail    #(rf/dispatch [:onboarding-2/biometrics-fail %])}})

(rf/defn biometrics-done
  {:events [:onboarding-2/biometrics-done]}
  [{:keys [db]}]
  {:db       (assoc-in db [:onboarding-2/profile :auth-method] constants/auth-method-biometric)
   :dispatch [:onboarding-2/create-account-and-login]})

(rf/defn biometrics-fail
  {:events [:onboarding-2/biometrics-fail]}
  [cofx code]
  (biometric/show-message cofx code))

(rf/defn create-account-and-login
  {:events [:onboarding-2/create-account-and-login]}
  [{:keys [db] :as cofx}]
  (let [{:keys [display-name seed-phrase password image-path color] :as profile}
        (:onboarding-2/profile db)]
    (rf/merge cofx
              {:dispatch       [:navigate-to :generating-keys]
               :dispatch-later [{:ms       constants/onboarding-generating-keys-animation-duration-ms
                                 :dispatch [:onboarding-2/navigate-to-identifiers]}]
               :db             (-> db
                                   (dissoc :profile/login)
                                   (dissoc :auth-method)
                                   (assoc :onboarding-2/new-account? true))}
              (if seed-phrase
                (profile.recover/recover-profile-and-login profile)
                (profile.create/create-profile-and-login profile)))))

(rf/defn on-delete-profile-success
  {:events [:onboarding-2/on-delete-profile-success]}
  [{:keys [db]} key-uid]
  (let [multiaccounts (dissoc (:profile/profiles-overview db) key-uid)]
    (merge
     {:db (assoc db :profile/profiles-overview multiaccounts)}
     (when-not (seq multiaccounts)
       {:set-root :intro}))))

(rf/defn password-set
  {:events [:onboarding-2/password-set]}
  [{:keys [db]} password]
  (let [supported-type (:biometric/supported-type db)]
    {:db       (-> db
                   (assoc-in [:onboarding-2/profile :password] password)
                   (assoc-in [:onboarding-2/profile :auth-method] constants/auth-method-password))
     :dispatch (if supported-type
                 [:navigate-to :enable-biometrics]
                 [:onboarding-2/create-account-and-login])}))

(rf/defn seed-phrase-entered
  {:events [:onboarding-2/seed-phrase-entered]}
  [_ seed-phrase on-error]
  {:multiaccount/validate-mnemonic [seed-phrase
                                    (fn [mnemonic key-uid]
                                      (re-frame/dispatch [:onboarding-2/seed-phrase-validated
                                                          mnemonic key-uid]))
                                    on-error]})

(rf/defn seed-phrase-validated
  {:events [:onboarding-2/seed-phrase-validated]}
  [{:keys [db]} seed-phrase key-uid]
  (if (contains? (:profile/profiles-overview db) key-uid)
    {:utils/show-confirmation
     {:title               (i18n/label :t/multiaccount-exists-title)
      :content             (i18n/label :t/multiaccount-exists-content)
      :confirm-button-text (i18n/label :t/unlock)
      :on-accept           (fn []
                             (re-frame/dispatch [:pop-to-root :profiles])
                             (re-frame/dispatch
                              [:profile/profile-selected key-uid]))
      :on-cancel           #(re-frame/dispatch [:pop-to-root :multiaccounts])}}
    {:db       (assoc-in db [:onboarding-2/profile :seed-phrase] seed-phrase)
     :dispatch [:navigate-to :create-profile]}))

(rf/defn navigate-to-create-profile
  {:events [:onboarding-2/navigate-to-create-profile]}
  [{:keys [db]}]
  ;; Restart the flow
  {:db       (dissoc db :onboarding-2/profile)
   :dispatch [:navigate-to :create-profile]})

(rf/defn onboarding-new-account-finalize-setup
  {:events [:onboarding-2/finalize-setup]}
  [{:keys [db]}]
  (let [masked-password    (get-in db [:onboarding-2/profile :password])
        key-uid            (get-in db [:profile/profile :key-uid])
        biometric-enabled? (= (get-in db [:onboarding-2/profile :auth-method])
                              constants/auth-method-biometric)]
    (cond-> {:db (assoc db :onboarding-2/generated-keys? true)}
      biometric-enabled?
      (assoc :keychain/save-password-and-auth-method
             {:key-uid         key-uid
              :masked-password masked-password
              :on-success      #(log/debug "successfully saved biometric")
              :on-error        #(log/error "failed to save biometrics"
                                           {:key-uid key-uid
                                            :error   %})}))))

(rf/defn navigate-to-identifiers
  {:events [:onboarding-2/navigate-to-identifiers]}
  [{:keys [db]}]
  (if (:onboarding-2/generated-keys? db)
    {:dispatch [:navigate-to :identifiers]}
    {:dispatch-later [{:ms       constants/onboarding-generating-keys-navigation-retry-ms
                       :dispatch [:onboarding-2/navigate-to-identifiers]}]}))
