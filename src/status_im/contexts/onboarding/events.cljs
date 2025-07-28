(ns status-im.contexts.onboarding.events
  (:require
    [quo.foundations.colors :as colors]
    status-im.common.biometric.events
    [status-im.constants :as constants]
    [status-im.contexts.onboarding.interceptors :as onboarding.interceptors]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.feature-flags :as ff]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn notifications-setup-start
  [{:keys [db]}
   [{:keys [enable-notifications?
            enable-news-notifications?
            biometrics?
            onboarding?
            syncing?]}]]
  {:db (if-not onboarding?
         db
         (update-in db
                    [:onboarding/profile]
                    assoc
                    :enable-notifications?      enable-notifications?
                    :enable-news-notifications? enable-news-notifications?))
   :fx (cond-> []
         (and (not onboarding?) enable-notifications? enable-news-notifications?)
         (conj [:dispatch [:notifications/news-notifications-switch true]])

         (and (not onboarding?) enable-notifications?)
         (conj [:dispatch [:push-notifications/switch true]])

         :always
         (conj [:dispatch
                [:onboarding/notifications-setup-done
                 {:onboarding? onboarding?
                  :syncing?    syncing?
                  :biometrics? biometrics?}]]))})

(rf/reg-event-fx :onboarding/notifications-setup-start notifications-setup-start)

(defn notifications-setup-done
  [{:keys [_db]} [{:keys [biometrics? onboarding? syncing?]}]]
  {:fx (cond-> []
         (and onboarding? syncing?)
         (conj [:dispatch [:onboarding/finalize-setup]])

         (and onboarding? syncing? (not biometrics?))
         (conj [:dispatch [:onboarding/finish-onboarding]])

         (and onboarding? (not syncing?))
         (conj [:dispatch [:onboarding/create-account-and-login]])

         (and (not onboarding?) (not syncing?))
         (conj [:dispatch [:shell/show-root-view]]))})


(rf/reg-event-fx :onboarding/notifications-setup-done notifications-setup-done)

(defn notifications-setup
  [{:keys [db]}
   [{:keys [biometrics-supported? biometrics? syncing? onboarding?]}]]
  (let [db-view-id      (get-in db [:view-id])
        current-view-id (if (= db-view-id :screen/onboarding.syncing-biometric)
                          :screen/onboarding.enable-biometrics
                          db-view-id)]
    {:db (assoc-in db [:onboarding/profile :notifications-prompted?] true)
     :fx [[:dispatch
           (if (ff/enabled? ::ff/settings.news-notifications)
             [:navigate-to-within-stack
              [:screen/onboarding.enable-notifications current-view-id]
              {:syncing?    syncing?
               :onboarding? onboarding?
               :biometrics? (and biometrics-supported? biometrics?)}]
             [:onboarding/notifications-setup-done
              {:onboarding? onboarding?
               :syncing?    syncing?
               :biometrics? (and biometrics-supported? biometrics?)}])]]}))

(rf/reg-event-fx :onboarding/notifications-setup notifications-setup)

(defn biometrics-setup-start
  [_ [{:keys [enable-biometrics? syncing?]}]]
  {:fx (cond-> []
         enable-biometrics?
         (conj [:dispatch
                [:onboarding/enable-biometrics
                 {:on-done [:onboarding/biometrics-setup-done
                            {:on-success [:onboarding/notifications-setup
                                          {:biometrics?           true
                                           :biometrics-supported? true
                                           :onboarding?           true
                                           :syncing?              syncing?}]
                             :on-fail    [:onboarding/biometrics-fail]}]}]])

         (not enable-biometrics?)
         (conj [:dispatch
                [:onboarding/notifications-setup
                 {:biometrics?           false
                  :biometrics-supported? true
                  :onboarding?           true
                  :syncing?              syncing?}]]))})

(rf/reg-event-fx :onboarding/biometrics-setup-start biometrics-setup-start)

(defn biometrics-setup-done
  [{:keys [db]} [{:keys [on-success on-fail]} {:keys [error]}]]
  {:db (assoc-in db [:onboarding/profile :auth-method] constants/auth-method-biometric)
   :fx [(if (some? error)
          [:dispatch (conj on-fail error)]
          [:dispatch on-success])]})

(rf/reg-event-fx :onboarding/biometrics-setup-done biometrics-setup-done)

(rf/reg-event-fx
 :onboarding/biometrics-fail
 (fn [_ [error]]
   {:dispatch [:biometric/show-message (ex-cause error)]}))

(rf/reg-event-fx :onboarding/enable-biometrics
 (fn [_ [{:keys [on-done]}]]
   {:fx [[:dispatch
          [:biometric/authenticate
           {:on-success #(rf/dispatch on-done)
            :on-fail    #(rf/dispatch (conj on-done %))}]]]}))

(rf/reg-event-fx :shell/show-root-view
 [onboarding.interceptors/local-profile-storage-interceptor]
 (fn [{:keys [db local-profile-storage]} [{:keys [notifications-prompt-skip?]}]]
   (let [{:keys [key-uid]}  (get-in db [:profile/profile])
         onboarding-profile (get-in db [:onboarding/profile])]
     (if (ff/enabled? ::ff/settings.news-notifications)
       (if (or (:notifications-prompted? local-profile-storage)
               (:notifications-prompted? onboarding-profile)
               notifications-prompt-skip?)
         {:fx (cond-> []
                (not (:notifications-prompted? local-profile-storage))
                (conj [:dispatch
                       [:profile/save-notifications-prompted
                        {:key-uid key-uid}]])
                :else
                (conj [:dispatch [:update-theme-and-init-root :screen/shell-stack]]
                      [:dispatch [:profile/toggle-testnet-mode-banner]]))}
         {:fx [[:dispatch
                [:profile/save-notifications-prompted
                 {:key-uid key-uid}]]
               [:dispatch
                [:onboarding/notifications-setup
                 {:biometrics? false
                  :syncing?    false
                  :onboarding? false}]]]})
       {:fx [[:dispatch [:update-theme-and-init-root :screen/shell-stack]]
             [:dispatch [:profile/toggle-testnet-mode-banner]]]}))))

(rf/reg-event-fx
 :onboarding/finish-onboarding
 (fn [_ []]
   {:fx [[:dispatch [:shell/change-tab shell.constants/default-selected-stack]]
         [:dispatch [:shell/show-root-view]]
         [:dispatch [:universal-links/process-stored-event]]]}))

(rf/reg-event-fx :onboarding/navigate-to-sign-in-by-seed-phrase
 (fn [{:keys [db]} [from-screen]]
   {:db (assoc db :onboarding/navigated-to-enter-seed-phrase-from-screen from-screen)
    :fx [[:dispatch
          [:navigate-to-within-stack [:screen/onboarding.enter-seed-phrase from-screen]
           {:on-success       #(rf/dispatch [:onboarding/seed-phrase-validated %])
            :onboarding-flow? true}]]]}))

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
    {:db (assoc db :profile/profiles-overview multiaccounts)
     :fx (cond-> []
           (ff/enabled? ::ff/settings.news-notifications)
           (conj [:dispatch
                  [:profile/remove-local-profile-storage
                   {:key-uid key-uid}]])
           (not (seq multiaccounts))
           (conj [:dispatch
                  [:update-theme-and-init-root :screen/onboarding.intro]]))}))

(rf/reg-event-fx
 :onboarding/password-set
 (fn [{:keys [db]} [masked-password]]
   (let [biometric-supported-type (get-in db [:biometrics :supported-type])
         syncing?                 (get-in db [:onboarding/profile :syncing?])
         from-screen              (get db
                                       :onboarding/navigated-to-enter-seed-phrase-from-screen
                                       :screen/onboarding.create-profile)]
     {:db (-> db
              (assoc-in [:onboarding/profile :password] masked-password)
              (assoc-in [:onboarding/profile :auth-method] constants/auth-method-password))
      :fx [[:dispatch
            (cond
              biometric-supported-type
              [:navigate-to-within-stack [:screen/onboarding.enable-biometrics from-screen]]

              (ff/enabled? ::ff/settings.news-notifications)
              [:onboarding/notifications-setup
               {:onboarding? true
                :biometrics? false
                :syncing?    syncing?}]

              :else
              [:onboarding/create-account-and-login])]]})))

(rf/reg-event-fx
 :onboarding/seed-phrase-validated
 (fn [{:keys [db]} [{:keys [seed-phrase key-uid]}]]
   (let [next-screen :screen/onboarding.create-profile-password
         from-screen (get db
                          :onboarding/navigated-to-enter-seed-phrase-from-screen
                          :screen/onboarding.create-profile)]
     (if (contains? (:profile/profiles-overview db) key-uid)
       {:fx [[:dispatch [:enter-seed-phrase/set-error (i18n/label :t/account-already-exist-error)]]]}
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
                 temporary-display-name?
                 enable-news-notifications?
                 enable-notifications?]} (:onboarding/profile db)
         {:keys [key-uid] :as profile}   (:profile/profile db)
         biometric-enabled?              (= auth-method constants/auth-method-biometric)]
     {:db (assoc db :onboarding/generated-keys? true)
      :fx (cond-> []
            temporary-display-name?
            (conj [:dispatch [:profile/set-default-profile-name profile]])

            biometric-enabled?
            (conj [:keychain/save-password-and-auth-method
                   {:key-uid         key-uid
                    :masked-password (if syncing?
                                       password
                                       (security/hash-masked-password password))
                    :on-success      (fn []
                                       (rf/dispatch [:onboarding/set-auth-method auth-method])
                                       (when syncing?
                                         (rf/dispatch
                                          [:onboarding/finish-onboarding])))
                    :on-error        #(log/error "failed to save biometrics"
                                                 {:key-uid key-uid
                                                  :error   %})}])

            (and enable-notifications?
                 enable-news-notifications?)
            (conj [:dispatch
                   [:notifications/news-notifications-switch true]])

            enable-notifications?
            (conj [:dispatch
                   [:push-notifications/switch true]]))})))
