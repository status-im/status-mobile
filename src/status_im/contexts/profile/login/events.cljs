(ns status-im.contexts.profile.login.events
  (:require
    [legacy.status-im.data-store.settings :as data-store.settings]
    [legacy.status-im.mailserver.core :as mailserver]
    [native-module.core :as native-module]
    [status-im.common.keychain.events :as keychain]
    [status-im.config :as config]
    [status-im.constants :as constants]
    status-im.contexts.profile.login.effects
    [status-im.contexts.profile.rpc :as profile.rpc]
    [status-im.feature-flags :as ff]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx :profile.login/login
 (fn [{:keys [db]}]
   (let [{:keys [key-uid password]} (:profile/login db)
         login-sha3-password        (native-module/sha3 (security/safe-unmask-data password))]
     {:db (-> db
              (assoc-in [:profile/login :processing] true)
              (assoc-in [:syncing :login-sha3-password] login-sha3-password))
      :fx [[:effects.profile/login [key-uid login-sha3-password]]]})))

(rf/reg-event-fx :profile.login/local-paired-user
 (fn [{:keys [db]}]
   (let [{:keys [key-uid password]} (get-in db [:syncing :profile])
         login-sha3-password        (get-in db [:syncing :login-sha3-password])
         password                   (if-not (nil? login-sha3-password) ;; already logged in
                                      login-sha3-password
                                      password)
         masked-password            (security/mask-data password)]
     {:db                    (-> db
                                 (assoc-in [:onboarding/profile :password] masked-password)
                                 (assoc-in [:onboarding/profile :syncing?] true))
      :effects.profile/login [key-uid password]})))

;; login phase 1: we want to load and show chats faster, so we split login into 2 phases
(rf/reg-event-fx :profile.login/login-existing-profile
 (fn [{:keys [db]} [settings account]]
   (let [{:networks/keys [_current-network _networks]
          :as            settings}
         (data-store.settings/rpc->settings settings)
         profile-overview (profile.rpc/rpc->profiles-overview account)
         log-level (or (:log-level settings) config/log-level)
         pairing-completed? (= (get-in db [:syncing :pairing-status]) :completed)]
     {:db (cond-> (-> db
                      (assoc :chats/loading?  true
                             :profile/profile (merge profile-overview
                                                     settings
                                                     {:log-level log-level}))
                      (assoc-in [:activity-center :loading?] true))
            pairing-completed?
            (dissoc :syncing))
      :fx (into [[:dispatch [:universal-links/generate-profile-url]]
                 [:dispatch [:community/fetch]]
                 [:dispatch [:wallet/initialize]]
                 [:push-notifications/load-preferences]
                 [:fetch-chats-preview
                  {:on-success (fn [result]
                                 (rf/dispatch [:chats-list/load-success result])
                                 (rf/dispatch [:communities/get-user-requests-to-join])
                                 (rf/dispatch [:profile.login/get-chats-callback]))}]
                 [:profile.config/get-node-config]
                 [:logs/set-level log-level]
                 [:activity-center.notifications/fetch-pending-contact-requests-fx]
                 [:activity-center/update-seen-state]
                 [:activity-center.notifications/fetch-unread-count]]

                (cond
                  pairing-completed?
                  [[:set-root :screen/onboarding.syncing-results]]

                  (get db :onboarding/new-account?)
                  [[:dispatch [:onboarding/finalize-setup]]]

                  :else
                  [[:profile.settings/switch-theme-fx
                    [(or (:appearance settings)
                         constants/theme-type-dark)
                     :shell-stack
                     false]]
                   [:set-root :shell-stack]
                   [:dispatch [:profile/show-testnet-mode-banner-if-enabled]]]))})))

;; login phase 2: we want to load and show chats faster, so we split login into 2 phases
(rf/reg-event-fx :profile.login/get-chats-callback
 (fn [{:keys [db]}]
   (let [{:keys [notifications-enabled? key-uid
                 preview-privacy?]} (:profile/profile db)]
     {:db db
      :fx [[:json-rpc/call
            [{:method     "wakuext_startMessenger"
              :on-success [:profile.login/messenger-started]
              :on-error   #(log/error
                            "failed to start messenger")}]]
           [:effects.profile/enable-local-notifications]
           [:contacts/initialize-contacts]
           [:browser/initialize-browser]
           [:dispatch [:mobile-network/on-network-status-change]]
           [:group-chats/get-group-chat-invitations]
           [:profile.settings/get-profile-picture key-uid]
           [:profile.settings/blank-preview-flag-changed preview-privacy?]
           [:chat.ui/request-link-preview-whitelist]
           [:visibility-status-updates/fetch]
           [:switcher-cards/fetch]
           (when (ff/enabled? ::ff/wallet.wallet-connect)
             [:dispatch [:wallet-connect/init]])
           (when-not (:universal-links/handling db)
             [:effects.chat/open-last-chat key-uid])
           (when notifications-enabled?
             [:effects/push-notifications-enable])]})))

(rf/reg-event-fx :profile.login/messenger-started
 (fn [{:keys [db]} [{:keys [mailservers]}]]
   (let [new-account? (get db :onboarding/new-account?)]
     {:db (-> db
              (assoc :messenger/started? true)
              (mailserver/add-mailservers mailservers))
      :fx [[:json-rpc/call
            [{:method     "admin_nodeInfo"
              :on-success [:profile.login/node-info-fetched]
              :on-error   #(log/error "node-info: failed error" %)}]]
           [:pairing/get-our-installations]
           (when-not new-account?
             [:dispatch [:universal-links/process-stored-event]])]})))

(rf/reg-event-fx :profile.login/node-info-fetched
 (fn [{:keys [db]} [node-info]]
   {:db (assoc db :node-info node-info)}))

(rf/reg-event-fx
 :profile.login/login-node-signal
 (fn [{{:onboarding/keys [recovered-account? new-account?] :as db} :db}
      [{:keys [settings account ensUsernames error]}]]
   (log/debug "[signals] node.login" "error" error)
   (if error
     {:db (update db :profile/login #(-> % (dissoc :processing) (assoc :error error)))}
     {:db (dissoc db :profile/login)
      :fx [[:logging/initialize-web3-client-version]
           (when (and new-account? (not recovered-account?))
             [:dispatch [:wallet-legacy/set-initial-blocks-range]])
           [:dispatch [:ens/update-usernames ensUsernames]]
           [:dispatch [:profile.login/login-existing-profile settings account]]]})))

(rf/reg-event-fx
 :profile.login/login-with-biometric-if-available
 (fn [_ [key-uid]]
   {:fx [[:effects.biometric/check-if-available
          {:key-uid    key-uid
           :on-success (fn [auth-method]
                         (rf/dispatch
                          [:profile.login/check-biometric-success
                           key-uid auth-method]))}]]}))

(rf/reg-event-fx
 :profile.login/check-biometric-success
 (fn [{:keys [db]} [key-uid auth-method]]
   {:db (assoc db :auth-method auth-method)
    :fx [(when (= auth-method keychain/auth-method-biometric)
           [:keychain/password-hash-migration
            {:key-uid  key-uid
             :callback (fn []
                         (rf/dispatch
                          [:biometric/authenticate
                           {:on-success #(rf/dispatch
                                          [:profile.login/biometric-success])
                            :on-fail    #(rf/dispatch
                                          [:profile.login/biometric-auth-fail %])}]))}])]}))

(rf/reg-event-fx
 :profile.login/get-user-password-success
 (fn [{:keys [db]} [password]]
   (when password
     {:db (-> db
              (assoc-in [:profile/login :password] password)
              (assoc-in [:profile/login :processing] true))
      :fx [[:set-root :progress]
           [:effects.profile/login
            [(get-in db [:profile/login :key-uid])
             (security/safe-unmask-data password)]]]})))

(rf/reg-event-fx
 :profile.login/biometric-success
 (fn [{:keys [db]}]
   (let [key-uid (get-in db [:profile/login :key-uid])]
     {:keychain/get-user-password [key-uid
                                   #(rf/dispatch [:profile.login/get-user-password-success %])]})))

(rf/reg-event-fx
 :profile.login/biometric-auth-fail
 (fn [_ [error]]
   (log/error (ex-message error)
              (-> error
                  ex-data
                  (assoc :code  (ex-cause error)
                         :event :profile.login/biometric-auth-fail)))
   {:dispatch [:biometric/show-message (ex-cause error)]}))

(rf/reg-event-fx
 :profile.login/verify-database-password
 (fn [_ [entered-password cb]]
   (let [hashed-password (-> entered-password
                             security/safe-unmask-data
                             native-module/sha3)]
     {:json-rpc/call [{:method     "accounts_verifyPassword"
                       :params     [hashed-password]
                       :on-success #(rf/dispatch [:profile.login/verified-database-password % cb])
                       :on-error   #(log/error "accounts_verifyPassword error" %)}]})))

(rf/reg-event-fx
 :profile.login/verified-database-password
 (fn [{:keys [db]} [valid? callback]]
   (if valid?
     (do
       (when (fn? callback)
         (callback))
       {:db (update db
                    :profile/login
                    dissoc
                    :processing :error)})
     {:db (update db
                  :profile/login
                  #(-> %
                       (dissoc :processing)
                       (assoc :error "Invalid password")))})))

(rf/reg-event-fx
 :profile/on-password-input-changed
 (fn [{:keys [db]} [{:keys [password error]}]]
   {:db (update db :profile/login assoc :password password :error error)}))

(rf/reg-event-fx
 :profile/show-testnet-mode-banner-if-enabled
 (fn [{:keys [db]}]
   (when (and (get-in db [:profile/profile :test-networks-enabled?])
              config/enable-alert-banner?)
     {:fx [[:dispatch
            [:alert-banners/add
             {:type :alert
              :text (i18n/label :t/testnet-mode-enabled)}]]]})))
