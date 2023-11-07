(ns status-im2.contexts.profile.login.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.browser.core :as browser]
    [status-im.communities.core :as communities]
    [status-im.data-store.chats :as data-store.chats]
    [status-im.data-store.settings :as data-store.settings]
    [status-im.data-store.switcher-cards :as switcher-cards-store]
    [status-im.data-store.visibility-status-updates :as visibility-status-updates-store]
    [status-im.group-chats.core :as group-chats]
    [status-im.mobile-sync-settings.core :as mobile-network]
    [status-im.transport.core :as transport]
    [status-im2.common.biometric.events :as biometric]
    [status-im2.common.keychain.events :as keychain]
    [status-im2.common.log :as logging]
    [status-im2.config :as config]
    [status-im2.contexts.chat.messages.link-preview.events :as link-preview]
    [status-im2.contexts.contacts.events :as contacts]
    [status-im2.contexts.profile.config :as profile.config]
    [status-im2.contexts.profile.rpc :as profile.rpc]
    [status-im2.contexts.profile.settings.events :as profile.settings.events]
    [status-im2.contexts.push-notifications.events :as notifications]
    [status-im2.contexts.shell.activity-center.events :as activity-center]
    [status-im2.contexts.wallet.events :as wallet]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(re-frame/reg-fx
 ::login
 (fn [[key-uid hashed-password]]
   ;;"node.login" signal will be triggered as a callback
   (native-module/login-account
    (assoc (profile.config/login) :keyUid key-uid :password hashed-password))))

(rf/defn login
  {:events [:profile.login/login]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (:profile/login db)]
    {:db     (assoc-in db [:profile/login :processing] true)
     ::login [key-uid (native-module/sha3 (security/safe-unmask-data password))]}))

(rf/defn login-local-paired-user
  {:events [:profile.login/local-paired-user]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (get-in db [:syncing :profile])]
    {::login [key-uid password]}))

(rf/defn redirect-to-root
  [{:keys [db] :as cofx}]
  (let [pairing-completed? (= (get-in db [:syncing :pairing-status]) :completed)]
    (cond
      pairing-completed?
      {:db       (dissoc db :syncing)
       :dispatch [:init-root :syncing-results]}

      (get db :onboarding-2/new-account?)
      {:dispatch [:onboarding-2/finalize-setup]}

      :else
      (rf/merge
       cofx
       (profile.settings.events/switch-theme nil :shell-stack)
       (navigation/init-root :shell-stack)))))

;; login phase 1, we want to load and show chats faster so we split login into 2 phases
(rf/defn login-existing-profile
  [{:keys [db] :as cofx} settings account]
  (let [{:networks/keys [current-network networks]
         :as            settings}
        (data-store.settings/rpc->settings settings)
        profile-overview (profile.rpc/rpc->profiles-overview account)]
    (rf/merge cofx
              {:db (assoc db
                          :chats/loading?           true
                          :networks/current-network current-network
                          :wallet/tokens-loading?   true
                          :networks/networks        (merge networks config/default-networks-by-id)
                          :profile/profile          (merge profile-overview settings))}
              (notifications/load-preferences)
              (data-store.chats/fetch-chats-preview
               {:on-success
                #(do (re-frame/dispatch [:chats-list/load-success %])
                     (rf/dispatch [:communities/get-user-requests-to-join])
                     (re-frame/dispatch [:profile.login/get-chats-callback]))})
              (profile.config/get-node-config)
              (communities/fetch)
              (communities/fetch-collapsed-community-categories)
              (communities/check-and-delete-pending-request-to-join)
              (logging/set-log-level (:log-level settings))
              (activity-center/notifications-fetch-pending-contact-requests)
              (activity-center/update-seen-state)
              (activity-center/notifications-fetch-unread-count)
              (wallet/get-ethereum-chains)
              (redirect-to-root))))

;; login phase 2, we want to load and show chats faster so we split login into 2 phases
(rf/defn get-chats-callback
  {:events [:profile.login/get-chats-callback]}
  [{:keys [db] :as cofx}]
  (let [{:networks/keys [current-network networks]} db
        {:keys [notifications-enabled?]}            (:profile/profile db)
        current-network-config                      (get networks current-network)
        network-id                                  (str (get-in networks
                                                                 [current-network :config :NetworkId]))]
    (rf/merge cofx
              (cond-> {:wallet-legacy/initialize-transactions-management-enabled nil
                       :wallet-legacy/initialize-wallet
                       [network-id
                        current-network-config
                        (fn [accounts tokens custom-tokens favourites]
                          (re-frame/dispatch [:wallet-legacy/initialize-wallet
                                              accounts tokens custom-tokens favourites]))]
                       :check-eip1559-activation {:network-id network-id}
                       :chat/open-last-chat (get-in db [:profile/profile :key-uid])}
                notifications-enabled?
                (assoc :effects/push-notifications-enable nil))
              (transport/start-messenger)
              (contacts/initialize-contacts)
              (browser/initialize-browser)
              (mobile-network/on-network-status-change)
              (group-chats/get-group-chat-invitations)
              (profile.settings.events/get-profile-picture)
              (profile.settings.events/change-preview-privacy)
              (link-preview/request-link-preview-whitelist)
              (visibility-status-updates-store/fetch-visibility-status-updates-rpc)
              (switcher-cards-store/fetch-switcher-cards-rpc))))

(rf/defn login-node-signal
  [{{:onboarding-2/keys [recovered-account? new-account?] :as db} :db :as cofx}
   {:keys [settings account ensUsernames error]}]
  (log/debug "[signals] node.login" "error" error)
  (if error
    {:db (update db :profile/login #(-> % (dissoc :processing) (assoc :error error)))}
    (rf/merge cofx
              {:db         (dissoc db :profile/login)
               :dispatch-n [[:logging/initialize-web3-client-version]
                            (when (and new-account? (not recovered-account?))
                              [:wallet-legacy/set-initial-blocks-range])
                            [:ens/update-usernames ensUsernames]]}
              (login-existing-profile settings account))))

(rf/defn login-with-biometric-if-available
  {:events [:profile.login/login-with-biometric-if-available]}
  [_ key-uid]
  {:keychain/get-auth-method [key-uid
                              #(rf/dispatch [:profile.login/get-auth-method-success % key-uid])]})

(rf/defn get-auth-method-success
  {:events [:profile.login/get-auth-method-success]}
  [{:keys [db]} auth-method]
  (merge {:db (assoc db :auth-method auth-method)}
         (when (= auth-method keychain/auth-method-biometric)
           {:biometric/authenticate
            {:on-success #(rf/dispatch [:profile.login/biometric-success])
             :on-faile   #(rf/dispatch [:profile.login/biometric-auth-fail])}})))

(rf/defn biometric-auth-success
  {:events [:profile.login/biometric-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:profile/login :key-uid])]
    (keychain/get-user-password cofx
                                key-uid
                                #(rf/dispatch [:profile.login/get-user-password-success %]))))

;; result of :keychain/get-auth-method above
(rf/defn get-user-password-success
  {:events [:profile.login/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (when password
    (rf/merge
     cofx
     {:db (assoc-in db [:profile/login :password] password)}
     (navigation/init-root :progress)
     (login))))

(rf/defn biometric-auth-fail
  {:events [:profile.login/biometric-auth-fail]}
  [{:keys [db] :as cofx} code]
  (rf/merge cofx
            (navigation/init-root :profiles)
            (biometric/show-message code)))

(rf/defn verify-database-password
  {:events [:profile.login/verify-database-password]}
  [_ entered-password cb]
  (let [hashed-password (-> entered-password
                            security/safe-unmask-data
                            native-module/sha3)]
    {:json-rpc/call [{:method     "accounts_verifyPassword"
                      :params     [hashed-password]
                      :on-success #(rf/dispatch [:profile.login/verified-database-password % cb])
                      :on-error   #(log/error "accounts_verifyPassword error" %)}]}))

(rf/defn verify-database-password-success
  {:events [:profile.login/verified-database-password]}
  [{:keys [db] :as cofx} valid? callback]
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
                      (assoc :error "Invalid password")))}))
