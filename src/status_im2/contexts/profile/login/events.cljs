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
    [status-im.mailserver.core :as mailserver]
    [status-im.mobile-sync-settings.core :as mobile-network]
    [status-im.pairing.core :as pairing]
    [status-im.stickers.core :as stickers]
    [status-im2.common.keychain.events :as keychain]
    [status-im2.common.log :as logging]
    [status-im2.common.universal-links :as universal-links]
    [status-im2.config :as config]
    [status-im2.contexts.chat.messages.link-preview.events :as link-preview]
    [status-im2.contexts.contacts.events :as contacts]
    [status-im2.contexts.profile.config :as profile.config]
    status-im2.contexts.profile.login.effects
    [status-im2.contexts.profile.rpc :as profile.rpc]
    [status-im2.contexts.profile.settings.events :as profile.settings.events]
    [status-im2.contexts.push-notifications.events :as notifications]
    [status-im2.contexts.shell.activity-center.events :as activity-center]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn login
  {:events [:profile.login/login]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (:profile/login db)
        login-sha3-password        (native-module/sha3 (security/safe-unmask-data password))]
    {:db                    (-> db
                                (assoc-in [:profile/login :processing] true)
                                (assoc-in [:syncing :login-sha3-password] login-sha3-password))
     :effects.profile/login [key-uid login-sha3-password]}))

(rf/defn biometrics-login
  {:events [:profile.login/biometrics-login]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (:profile/login db)]
    {:db                    (assoc-in db [:profile/login :processing] true)
     :effects.profile/login [key-uid (security/safe-unmask-data password)]}))

(rf/defn login-local-paired-user
  {:events [:profile.login/local-paired-user]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (get-in db [:syncing :profile])
        login-sha3-password        (get-in db [:syncing :login-sha3-password])
        password                   (if-not (nil? login-sha3-password) ;; already logged in
                                     login-sha3-password
                                     password)
        masked-password            (security/mask-data password)]
    {:db                    (-> db
                                (assoc-in [:onboarding/profile :password] masked-password)
                                (assoc-in [:onboarding/profile :syncing?] true))
     :effects.profile/login [key-uid password]}))

(rf/defn redirect-to-root
  [{:keys [db] :as cofx}]
  (let [pairing-completed? (= (get-in db [:syncing :pairing-status]) :completed)]
    (cond
      pairing-completed?
      {:db       (dissoc db :syncing)
       :dispatch [:init-root :syncing-results]}

      (get db :onboarding/new-account?)
      {:dispatch [:onboarding/finalize-setup]}

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
              {:db (-> db
                       (assoc :chats/loading?           true
                              :networks/current-network current-network
                              :networks/networks        (merge networks config/default-networks-by-id)
                              :profile/profile          (merge profile-overview settings))
                       (assoc-in [:wallet :ui :tokens-loading?] true))
               :fx [[:dispatch [:wallet/get-ethereum-chains]]
                    [:dispatch [:universal-links/generate-profile-url]]]}
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
    (rf/merge
     cofx
     (cond-> {:json-rpc/call                              [{:method     "wakuext_startMessenger"
                                                            :on-success #(re-frame/dispatch
                                                                          [:messenger-started %])
                                                            :on-error   #(log/error
                                                                          "failed to start messenger")}]
              :check-eip1559-activation                   {:network-id network-id}
              :effects.profile/enable-local-notifications nil
              :dispatch-n                                 [[:wallet/get-accounts]]}
       (not (:universal-links/handling db))
       (assoc :effects.chat/open-last-chat (get-in db [:profile/profile :key-uid]))
       notifications-enabled?
       (assoc :effects/push-notifications-enable nil))
     (contacts/initialize-contacts)
     (browser/initialize-browser)
     (mobile-network/on-network-status-change)
     (group-chats/get-group-chat-invitations)
     (profile.settings.events/get-profile-picture)
     (profile.settings.events/change-preview-privacy)
     (link-preview/request-link-preview-whitelist)
     (visibility-status-updates-store/fetch-visibility-status-updates-rpc)
     (switcher-cards-store/fetch-switcher-cards-rpc))))

(rf/defn messenger-started
  {:events [:messenger-started]}
  [{:keys [db] :as cofx} {:keys [mailservers] :as response}]
  (log/info "Messenger started")
  (let [new-account? (get db :onboarding-2/new-account?)]
    (rf/merge cofx
              {:db            (-> db
                                  (assoc :messenger/started? true)
                                  (mailserver/add-mailservers mailservers))
               :json-rpc/call [{:method     "admin_nodeInfo"
                                :on-success #(re-frame/dispatch [:node-info-fetched %])
                                :on-error   #(log/error "node-info: failed error" %)}]}
              (pairing/init)
              (stickers/load-packs)
              (when-not new-account?
                (universal-links/process-stored-event)))))

(rf/defn set-node-info
  {:events [:node-info-fetched]}
  [{:keys [db]} node-info]
  {:db (assoc db :node-info node-info)})

(rf/defn login-node-signal
  [{{:onboarding/keys [recovered-account? new-account?] :as db} :db :as cofx}
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
  [{:keys [db]} auth-method key-uid]
  (merge {:db (assoc db :auth-method auth-method)}
         (when (= auth-method keychain/auth-method-biometric)
           {:keychain/password-hash-migration
            {:key-uid  key-uid
             :callback (fn []
                         (rf/dispatch [:biometric/authenticate
                                       {:on-success #(rf/dispatch [:profile.login/biometric-success])
                                        :on-fail    #(rf/dispatch
                                                      [:profile.login/biometric-auth-fail %])}]))}})))

;; result of :keychain/get-auth-method above
(rf/defn get-user-password-success
  {:events [:profile.login/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (when password
    (rf/merge
     cofx
     {:db (assoc-in db [:profile/login :password] password)}
     (navigation/init-root :progress)
     (biometrics-login))))

(rf/reg-event-fx
 :profile.login/biometric-success
 (fn [{:keys [db]}]
   (let [key-uid (get-in db [:profile/login :key-uid])]
     {:db db
      :fx [[:biometric/reset-not-enrolled-error key-uid]
           [:keychain/get-user-password
            [key-uid #(rf/dispatch [:profile.login/get-user-password-success %])]]]})))

(rf/reg-event-fx
 :profile.login/biometric-auth-fail
 (fn [{:keys [db]} [code]]
   (let [key-uid (get-in db [:profile/login :key-uid])]
     {:db db
      :fx [[:dispatch [:init-root :profiles]]
           (if (= code "NOT_ENROLLED")
             [:biometric/supress-not-enrolled-error
              [key-uid
               [:biometric/show-message code]]]
             [:dispatch [:biometric/show-message code]])]})))


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
  [{:keys [db]} valid? callback]
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
