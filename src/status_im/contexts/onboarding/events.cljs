(ns status-im.contexts.onboarding.events
  (:require
    [re-frame.core :as re-frame]
    status-im.common.biometric.events
    [status-im.constants :as constants]
    [status-im.contexts.profile.create.events :as profile.create]
    [status-im.contexts.profile.recover.events :as profile.recover]
    [status-im.contexts.shell.constants :as shell.constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx
 :onboarding/finish-onboarding
 (fn [_ [notifications-enabled?]]
   {:fx [(when notifications-enabled?
           [:dispatch [:push-notifications/switch true]])
         [:dispatch [:shell/change-tab shell.constants/default-selected-stack]]
         [:dispatch [:update-theme-and-init-root :shell-stack]]
         [:dispatch [:profile/show-testnet-mode-banner-if-enabled]]
         [:dispatch [:universal-links/process-stored-event]]]}))

(rf/reg-event-fx
 :onboarding/profile-data-set
 (fn [{:keys [db]} [onboarding-data]]
   (let [navigate-from-screen (get db
                                   :onboarding/navigated-to-enter-seed-phrase-from-screen
                                   :screen/onboarding.new-to-status)]
     {:db (update db :onboarding/profile merge onboarding-data)
      :fx [[:dispatch
            [:navigate-to-within-stack
             [:screen/onboarding.create-profile-password navigate-from-screen]]]]})))

(rf/defn enable-biometrics
  {:events [:onboarding/enable-biometrics]}
  [_]
  {:fx [[:dispatch
         [:biometric/authenticate
          {:on-success #(rf/dispatch [:onboarding/biometrics-done])
           :on-fail    #(rf/dispatch [:onboarding/biometrics-fail %])}]]]})

(rf/reg-event-fx :onboarding/navigate-to-sign-in-by-seed-phrase
 (fn [{:keys [db]} [from-screen]]
   {:db (assoc db :onboarding/navigated-to-enter-seed-phrase-from-screen from-screen)
    :fx [[:dispatch [:navigate-to-within-stack [:screen/onboarding.enter-seed-phrase from-screen]]]]}))

(rf/reg-event-fx
 :onboarding/clear-navigated-to-enter-seed-phrase-from-screen
 (fn [{:keys [db]}]
   {:db (dissoc db :onboarding/navigated-to-enter-seed-phrase-from-screen)}))

(rf/reg-event-fx :onboarding/navigate-to-enable-notifications-from-syncing
 (fn [{:keys [db]}]
   {:db       (dissoc db :onboarding/profile)
    :dispatch [:navigate-to-within-stack
               [:screen/onboarding.enable-notifications :screen/onboarding.enable-biometrics]]}))

(rf/reg-event-fx :onboarding/navigate-to-enable-notifications
 (fn [{:keys [db]}]
   {:dispatch [:navigate-to-within-stack
               [:screen/onboarding.enable-notifications
                (get db
                     :onboarding/navigated-to-enter-seed-phrase-from-screen
                     :screen/onboarding.new-to-status)]]}))

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
  (let [{:keys [seed-phrase]
         :as   profile}            (:onboarding/profile db)
        syncing-account-recovered? (and (seq (:syncing/key-uid db))
                                        (= (:syncing/key-uid db)
                                           (get-in db [:onboarding/profile :key-uid])))]
    (rf/merge cofx
              {:fx [[:dispatch
                     [:navigate-to-within-stack
                      [:screen/onboarding.preparing-status
                       (get db
                            :onboarding/navigated-to-enter-seed-phrase-from-screen
                            :screen/onboarding.new-to-status)]]]
                    (when-not syncing-account-recovered?
                      [:dispatch [:syncing/clear-syncing-installation-id]])]
               :db (-> db
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
       {:dispatch [:update-theme-and-init-root :screen/onboarding.intro]}))))

(rf/reg-event-fx
 :onboarding/password-set
 (fn [{:keys [db]} [masked-password]]
   (let [biometric-supported-type (get-in db [:biometrics :supported-type])]
     {:db (-> db
              (assoc-in [:onboarding/profile :password] masked-password)
              (assoc-in [:onboarding/profile :auth-method] constants/auth-method-password))
      :fx [[:dispatch
            (if biometric-supported-type
              [:navigate-to-within-stack
               [:screen/onboarding.enable-biometrics
                (get db
                     :onboarding/navigated-to-enter-seed-phrase-from-screen
                     :screen/onboarding.new-to-status)]]
              [:onboarding/create-account-and-login])]]})))

(rf/defn navigate-to-enable-biometrics
  {:events [:onboarding/navigate-to-enable-biometrics]}
  [{:keys [db]}]
  (let [supported-type (get-in db [:biometrics :supported-type])]
    {:dispatch (if supported-type
                 [:open-modal :screen/onboarding.enable-biometrics]
                 [:open-modal :screen/onboarding.enable-notifications])}))

(rf/defn seed-phrase-validated
  {:events [:onboarding/seed-phrase-validated]}
  [{:keys [db]} seed-phrase key-uid]
  (let [syncing-account-recovered? (and (seq (:syncing/key-uid db))
                                        (= (:syncing/key-uid db) key-uid))
        next-screen                (if syncing-account-recovered?
                                     :screen/onboarding.create-profile-password
                                     :screen/onboarding.create-profile)]
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
      {:db (-> db
               (assoc-in [:onboarding/profile :seed-phrase] seed-phrase)
               (assoc-in [:onboarding/profile :key-uid] key-uid)
               (assoc-in [:onboarding/profile :color] constants/profile-default-color))
       :fx [[:dispatch
             [:navigate-to-within-stack
              [next-screen
               (get db
                    :onboarding/navigated-to-enter-seed-phrase-from-screen
                    :screen/onboarding.new-to-status)]]]]})))

(rf/defn navigate-to-create-profile
  {:events [:onboarding/navigate-to-create-profile]}
  [{:keys [db]}]
  ;; Restart the flow
  {:db       (dissoc db :onboarding/profile)
   :dispatch [:navigate-to-within-stack
              [:screen/onboarding.create-profile :screen/onboarding.new-to-status]]})

(rf/reg-event-fx :onboarding/navigate-to-sign-in-by-syncing
 (fn [{:keys [db]}]
   ;; Restart the flow
   {:db       (dissoc db :onboarding/profile)
    :dispatch [:navigate-to-within-stack
               [:screen/onboarding.sign-in-intro :screen/onboarding.sync-or-recover-profile]]}))

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
        biometric-enabled? (= auth-method constants/auth-method-biometric)]
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
                                   (rf/dispatch
                                    [:onboarding/navigate-to-enable-notifications-from-syncing])))
              :on-error        #(log/error "failed to save biometrics"
                                           {:key-uid key-uid
                                            :error   %})}))))

