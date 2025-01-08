(ns status-im.contexts.onboarding.events
  (:require
    [quo.foundations.colors :as colors]
    [re-frame.core :as re-frame]
    status-im.common.biometric.events
    [status-im.constants :as constants]
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

(rf/reg-event-fx :onboarding/navigate-to-enable-notifications
 (fn [{:keys [db]}]
   {:dispatch [:navigate-to-within-stack
               [:screen/onboarding.enable-notifications
                (get db
                     :onboarding/navigated-to-enter-seed-phrase-from-screen
                     :screen/onboarding.create-profile)]]}))

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

(rf/reg-event-fx :onboarding/create-account-and-login
 (fn [{:keys [db]}]
   (let [{:keys [seed-phrase]
          :as   profile}            (:onboarding/profile db)
         syncing-account-recovered? (and (seq (:syncing/key-uid db))
                                         (= (:syncing/key-uid db)
                                            (get-in db [:onboarding/profile :key-uid])))]
     {:db (-> db
              (dissoc :profile/login)
              (dissoc :auth-method)
              (assoc :onboarding/new-account? true))
      :fx [[:dispatch
            [:navigate-to-within-stack
             [:screen/onboarding.preparing-status
              (get db
                   :onboarding/navigated-to-enter-seed-phrase-from-screen
                   :screen/onboarding.create-profile)]]]
           (when-not syncing-account-recovered?
             [:dispatch [:syncing/clear-syncing-installation-id]])
           (if seed-phrase
             [:dispatch [:profile.recover/recover-and-login profile]]
             [:dispatch [:profile.create/create-and-login profile]])]})))

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
   (let [biometric-supported-type (get-in db [:biometrics :supported-type])
         from-screen              (get db
                                       :onboarding/navigated-to-enter-seed-phrase-from-screen
                                       :screen/onboarding.create-profile)]
     {:db (-> db
              (assoc-in [:onboarding/profile :password] masked-password)
              (assoc-in [:onboarding/profile :auth-method] constants/auth-method-password))
      :fx [[:dispatch
            (if biometric-supported-type
              [:navigate-to-within-stack [:screen/onboarding.enable-biometrics from-screen]]
              [:onboarding/create-account-and-login])]]})))

(rf/reg-event-fx
 :onboarding/seed-phrase-validated
 (fn [{:keys [db]} [seed-phrase key-uid]]
   (let [next-screen :screen/onboarding.create-profile-password
         from-screen (get db
                          :onboarding/navigated-to-enter-seed-phrase-from-screen
                          :screen/onboarding.create-profile)]
     (if (contains? (:profile/profiles-overview db) key-uid)
       {:fx [[:effects.utils/show-confirmation
              {:title               (i18n/label :t/multiaccount-exists-title)
               :content             (i18n/label :t/multiaccount-exists-content)
               :confirm-button-text (i18n/label :t/unlock)
               :on-accept           (fn []
                                      (re-frame/dispatch [:pop-to-root :screen/profile.profiles])
                                      (re-frame/dispatch [:profile/profile-selected key-uid]))
               :on-cancel           #(re-frame/dispatch [:pop-to-root :multiaccounts])}]]}
       {:db (-> db
                (assoc-in [:onboarding/profile :seed-phrase] seed-phrase)
                (assoc-in [:onboarding/profile :key-uid] key-uid)
                (assoc-in [:onboarding/profile :color] constants/profile-default-color))
        :fx [[:dispatch [:navigate-to-within-stack [next-screen from-screen]]]]}))))

(rf/reg-event-fx
 :onboarding/navigate-to-create-profile-password
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc-in [:onboarding/profile :color] (rand-nth colors/account-colors))
            (update :onboarding/profile dissoc :image-path))
    :fx [[:dispatch
          [:navigate-to-within-stack
           [:screen/onboarding.create-profile-password :screen/onboarding.create-profile]]]]}))

(rf/reg-event-fx :onboarding/navigate-to-sign-in-by-syncing
 (fn [{:keys [db]}]
   ;; Restart the flow
   {:db       (dissoc db :onboarding/profile)
    :dispatch [:navigate-to-within-stack
               [:screen/onboarding.sign-in-intro :screen/onboarding.log-in]]}))

(rf/reg-event-fx :onboarding/set-auth-method
 (fn [{:keys [db]} [auth-method]]
   {:db (assoc db :auth-method auth-method)}))

(def ^:const temp-display-name
  "While creating a profile, we cannot use an empty string; this value works as a
  placeholder that will be updated later once the compressed key exists. See
  `status-im.contexts.profile.edit.name.events/get-default-display-name` for more details."
  "temporal username")

(rf/reg-event-fx
 :onboarding/use-temporary-display-name
 (fn [{:keys [db]} [temporary-display-name?]]
   {:db (assoc db
               :onboarding/profile
               {:temporary-display-name? temporary-display-name?
                :display-name            (if temporary-display-name?
                                           temp-display-name
                                           "")})}))

(rf/reg-event-fx
 :onboarding/finalize-setup
 (fn [{db :db}]
   (let [{:keys [password syncing? auth-method
                 temporary-display-name?]} (:onboarding/profile db)
         {:keys [key-uid] :as profile}     (:profile/profile db)
         biometric-enabled?                (= auth-method constants/auth-method-biometric)]
     {:db (assoc db :onboarding/generated-keys? true)
      :fx [(when temporary-display-name?
             [:dispatch [:profile/set-default-profile-name profile]])
           (when biometric-enabled?
             [:keychain/save-password-and-auth-method
              {:key-uid         key-uid
               :masked-password (if syncing?
                                  password
                                  (security/hash-masked-password password))
               :on-success      (fn []
                                  (rf/dispatch [:onboarding/set-auth-method auth-method])
                                  (when syncing?
                                    (rf/dispatch
                                     [:onboarding/finish-onboarding false])))
               :on-error        #(log/error "failed to save biometrics"
                                            {:key-uid key-uid
                                             :error   %})}])]})))
