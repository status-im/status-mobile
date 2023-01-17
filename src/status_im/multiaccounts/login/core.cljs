(ns status-im.multiaccounts.login.core
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [status-im.async-storage.core :as async-storage]
   [status-im.chat.models.link-preview :as link-preview]
   [status-im.communities.core :as communities]
   [status-im.contact.core :as contact]
   [status-im.data-store.chats :as data-store.chats]
   [status-im.data-store.invitations :as data-store.invitations]
   [status-im.data-store.settings :as data-store.settings]
   [status-im.data-store.switcher-cards :as switcher-cards-store]
   [status-im.data-store.visibility-status-updates :as visibility-status-updates-store]
   [status-im.ethereum.core :as ethereum]
   [status-im.ethereum.eip55 :as eip55]
   [status-im.ethereum.tokens :as tokens]
   [status-im.ethereum.transactions.core :as transactions]
   [status-im.fleet.core :as fleet]
   [i18n.i18n :as i18n]
   [status-im.keycard.common :as keycard.common]
   [status-im.mobile-sync-settings.core :as mobile-network]
   [status-im.multiaccounts.biometric.core :as biometric]
   [status-im.multiaccounts.core :as multiaccounts]
   [status-im.native-module.core :as status]
   [status-im.node.core :as node]
   [status-im.notifications.core :as notifications]
   [status-im.popover.core :as popover]
   [status-im.signing.eip1559 :as eip1559]
   [status-im.transport.core :as transport]
   [status-im.ui.components.react :as react]
   [status-im.utils.config :as config]
   [utils.re-frame :as rf]
   [status-im.utils.keychain.core :as keychain]
   [status-im.utils.mobile-sync :as utils.mobile-sync]
   [status-im.utils.platform :as platform]
   [status-im.utils.types :as types]
   [status-im.utils.utils :as utils]
   [status-im.utils.wallet-connect :as wallet-connect]
   [status-im.wallet-connect-legacy.core :as wallet-connect-legacy]
   [status-im.wallet.core :as wallet]
   [status-im.wallet.prices :as prices]
   [status-im2.common.json-rpc.events :as json-rpc]
   [status-im2.contexts.activity-center.events :as activity-center]
   [status-im2.navigation.events :as navigation]
   [status-im2.setup.log :as logging]
   [taoensso.timbre :as log]
   [utils.security.core :as security]))

(re-frame/reg-fx
 ::initialize-communities-enabled
 (fn []
   (let [callback #(re-frame/dispatch [:multiaccounts.ui/switch-communities-enabled %])]
     (if config/communities-enabled?
       (callback true)
       (async-storage/get-item
        :communities-enabled?
        callback)))))

(re-frame/reg-fx
 ::initialize-transactions-management-enabled
 (fn []
   (let [callback #(re-frame/dispatch [:multiaccounts.ui/switch-transactions-management-enabled %])]
     (async-storage/get-item
      :transactions-management-enabled?
      callback))))

(re-frame/reg-fx
 ::login
 (fn [[key-uid account-data hashed-password]]
   (status/login-with-config key-uid account-data hashed-password node/login-node-config)))

(re-frame/reg-fx
 ::export-db
 (fn [[key-uid account-data hashed-password callback]]
   (status/export-db key-uid account-data hashed-password callback)))

(re-frame/reg-fx
 ::import-db
 (fn [[key-uid account-data hashed-password]]
   (status/import-db key-uid account-data hashed-password)))

(re-frame/reg-fx
 ::enable-local-notifications
 (fn []
   (status/start-local-notifications)))

(re-frame/reg-fx
 ::initialize-wallet-connect
 (fn []
   (wallet-connect/init
    #(re-frame/dispatch [:wallet-connect/client-init %])
    #(log/error "[wallet-connect]" %))))

(defn rpc->accounts
  [accounts]
  (reduce (fn [acc {:keys [chat type wallet] :as account}]
            (if chat
              acc
              (let [account (cond-> (update account
                                            :address
                                            eip55/address->checksum)
                              type
                              (update :type keyword))]
                ;; if the account is the default wallet we
                ;; put it first in the list
                (if wallet
                  (into [account] acc)
                  (conj acc account)))))
          []
          accounts))

;;TODO remove this code after all invalid names will be fixed (ask chu or flexsurfer)
(def invalid-addrr
  #{"0x9575cf381f71368a54e09b8138ebe046a1ef31ce93e6c37661513b21faaf741e"
    "0x56fa5de8cd4f2a3cbc122e7c51ac8690c6fc739b7c3724add97d0c55cc783d45"
    "0xf0e49d178fa34ac3ade4625e144f51e5f982434f0912bcbe23b6467343f48305"
    "0x60d1bf67e9d0d34368a6422c55f034230cc0997b186dd921fd18e89b7f0df5f2"
    "0x5fe69d562990616a02f4a5f934aa973b27bf02c4fc774f9ad82f105379f16789"
    "0xf1cabf2d74576ef76dfcb1182fd59a734a26c95ea6e68fc8f91ca4bfa1ea0594"
    "0x21d8ce6c0e32481506f98218920bee88f03d9c1b45dab3546948ccc34b1aadea"
    "0xbf7a74b39090fb5b1366f61fb4ac3ecc4b7f99f0fd3cb326dc5c18c58d58d7b6"
    "0xeeb570494d442795235589284100812e4176e9c29d151a81df43b6286ef66c49"
    "0x86a12d91c813f69a53349ff640e32af97d5f5c1f8d42d54cf4c8aa8dea231955"
    "0x0011a30f5b2023bc228f6dd5608b3e7149646fa83f33350926ceb1925af78f08"})

(rf/defn check-invalid-ens
  [{:keys [db]}]
  (async-storage/get-item
   :invalid-ens-name-seen
   (fn [already-seen]
     (when (and (not already-seen)
                (boolean (get invalid-addrr
                              (ethereum/sha3 (string/lower-case (ethereum/default-address db))))))
       (utils/show-popup
        (i18n/label :t/warning)
        (i18n/label :t/ens-username-invalid-name-warning)
        #(async-storage/set-item! :invalid-ens-name-seen true)))))
  nil)

(rf/defn initialize-wallet
  {:events [::initialize-wallet]}
  [{:keys [db] :as cofx} accounts tokens custom-tokens
   favourites scan-all-tokens? new-account?]
  (rf/merge
   cofx
   {:db                          (assoc db
                                        :multiaccount/accounts
                                        (rpc->accounts accounts))
    ;; NOTE: Local notifications should be enabled only after wallet was started
    ::enable-local-notifications nil}
   (check-invalid-ens)
   (wallet/initialize-tokens tokens custom-tokens)
   (wallet/initialize-favourites favourites)
   (wallet/get-pending-transactions)
   (wallet/fetch-collectibles-collection)
   (cond
     (and new-account?
          (not scan-all-tokens?))
     (wallet/set-zero-balances (first accounts))

     (and new-account?
          scan-all-tokens?
          (not (utils.mobile-sync/cellular? (:network/type db))))
     (wallet/set-max-block (get (first accounts) :address) 0)

     :else
     (wallet/get-cached-balances scan-all-tokens?))
   (when-not (get db :wallet/new-account)
     (wallet/restart-wallet-service nil))
   (when (or (not (utils.mobile-sync/syncing-allowed? cofx))
             (ethereum/binance-chain? db))
     (transactions/get-fetched-transfers))
   (when (ethereum/binance-chain? db)
     (wallet/request-current-block-update))
   (prices/update-prices)
   (wallet-connect-legacy/get-connector-session-from-db)))

(rf/defn login
  {:events [:multiaccounts.login.ui/password-input-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name identicon]} (:multiaccounts/login db)]
    {:db     (-> db
                 (assoc-in [:multiaccounts/login :processing] true)
                 (dissoc :intro-wizard :recovered-account?)
                 (update :keycard dissoc :flow))
     ::login [key-uid
              (types/clj->json {:name      name
                                :key-uid   key-uid
                                :identicon identicon})
              (ethereum/sha3 (security/safe-unmask-data password))]}))

(rf/defn export-db-submitted
  {:events [:multiaccounts.login.ui/export-db-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name identicon]} (:multiaccounts/login db)]
    {::export-db [key-uid
                  (types/clj->json {:name      name
                                    :key-uid   key-uid
                                    :identicon identicon})
                  (ethereum/sha3 (security/safe-unmask-data password))
                  (fn [path]
                    (when platform/ios?
                      (let [uri (str "file://" path)]
                        (.share ^js react/sharing
                                (clj->js {:title "Unencrypted database"
                                          :url   uri})))))]}))

(rf/defn import-db-submitted
  {:events [:multiaccounts.login.ui/import-db-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name identicon]} (:multiaccounts/login db)]
    {::import-db [key-uid
                  (types/clj->json {:name      name
                                    :key-uid   key-uid
                                    :identicon identicon})
                  (ethereum/sha3 (security/safe-unmask-data password))]}))

(rf/defn finish-keycard-setup
  [{:keys [db] :as cofx}]
  {:db (update db :keycard dissoc :flow)})

(rf/defn initialize-dapp-permissions
  {:events [::initialize-dapp-permissions]}
  [{:keys [db]} all-dapp-permissions]
  (let [dapp-permissions (reduce (fn [acc {:keys [dapp] :as dapp-permissions}]
                                   (assoc acc dapp dapp-permissions))
                                 {}
                                 all-dapp-permissions)]
    {:db (assoc db :dapps/permissions dapp-permissions)}))

(rf/defn initialize-browsers
  {:events [::initialize-browsers]}
  [{:keys [db]} all-stored-browsers]
  (let [browsers (reduce (fn [acc {:keys [browser-id] :as browser}]
                           (assoc acc browser-id browser))
                         {}
                         all-stored-browsers)]
    {:db (assoc db :browser/browsers browsers)}))

(rf/defn initialize-bookmarks
  {:events [::initialize-bookmarks]}
  [{:keys [db]} stored-bookmarks]
  (let [bookmarks (reduce (fn [acc {:keys [url] :as bookmark}]
                            (assoc acc url bookmark))
                          {}
                          stored-bookmarks)]
    {:db (assoc db :bookmarks/bookmarks bookmarks)}))

(rf/defn initialize-invitations
  {:events [::initialize-invitations]}
  [{:keys [db]} invitations]
  {:db (assoc db
              :group-chat/invitations
              (reduce (fn [acc {:keys [id] :as inv}]
                        (assoc acc id (data-store.invitations/<-rpc inv)))
                      {}
                      invitations))})

(rf/defn initialize-web3-client-version
  {:events [::initialize-web3-client-version]}
  [{:keys [db]} node-version]
  {:db (assoc db :web3-node-version node-version)})

(rf/defn handle-close-app-confirmed
  {:events [::close-app-confirmed]}
  [_]
  {:ui/close-application nil})

(rf/defn check-network-version
  [_ network-id]
  {:json-rpc/call
   [{:method     "net_version"
     :on-success
     (fn [fetched-network-id]
       (when (not= network-id (str (int fetched-network-id)))
         ;;TODO: this shouldn't happen but in case it does
         ;;we probably want a better error message
         (utils/show-popup
          (i18n/label :t/ethereum-node-started-incorrectly-title)
          (i18n/label :t/ethereum-node-started-incorrectly-description
                      {:network-id         network-id
                       :fetched-network-id fetched-network-id})
          #(re-frame/dispatch [::close-app-confirmed]))))}]})

(defn normalize-tokens
  [network-id tokens]
  (mapv #(-> %
             (update :symbol keyword)
             ((partial tokens/update-icon (ethereum/chain-id->chain-keyword (int network-id)))))
        tokens))

(re-frame/reg-fx
 ::get-tokens
 (fn [[network-id accounts recovered-account?]]
   (utils/set-timeout
    (fn []
      (json-rpc/call {:method     "wallet_getTokens"
                      :params     [(int network-id)]
                      :on-success #(re-frame/dispatch [::initialize-wallet
                                                       accounts
                                                       (normalize-tokens network-id %)
                                                       nil nil
                                                       recovered-account?
                                                       true])}))
    2000)))

(re-frame/reg-fx
 ;;TODO: this could be replaced by a single API call on status-go side
 ::initialize-wallet
 (fn [[network-id callback]]
   (-> (js/Promise.all
        (clj->js
         [(js/Promise.
           (fn [resolve reject]
             (json-rpc/call {:method     "accounts_getAccounts"
                             :on-success resolve
                             :on-error   reject})))
          (js/Promise.
           (fn [resolve _]
             (json-rpc/call {:method     "wallet_getTokens"
                             :params     [(int network-id)]
                             :on-success resolve
                             :on-error   #(resolve nil)})))
          (js/Promise.
           (fn [resolve reject]
             (json-rpc/call {:method     "wallet_getCustomTokens"
                             :on-success resolve
                             :on-error   reject})))
          (js/Promise.
           (fn [resolve reject]
             (json-rpc/call {:method     "wallet_getSavedAddresses"
                             :on-success resolve
                             :on-error   reject})))]))
       (.then (fn [[accounts tokens custom-tokens favourites]]
                (callback accounts
                          (normalize-tokens network-id tokens)
                          (mapv #(update % :symbol keyword) custom-tokens)
                          (filter :favourite favourites))))
       (.catch (fn [_]
                 (log/error "Failed to initialize wallet"))))))

(rf/defn initialize-browser
  [_]
  {:json-rpc/call
   [{:method     "wakuext_getBrowsers"
     :on-success #(re-frame/dispatch [::initialize-browsers %])}
    {:method     "browsers_getBookmarks"
     :on-success #(re-frame/dispatch [::initialize-bookmarks %])}
    {:method     "permissions_getDappPermissions"
     :on-success #(re-frame/dispatch [::initialize-dapp-permissions %])}]})

(rf/defn initialize-appearance
  [cofx]
  {:multiaccounts.ui/switch-theme (get-in cofx [:db :multiaccount :appearance])})

(rf/defn get-group-chat-invitations
  [_]
  {:json-rpc/call
   [{:method     "wakuext_getGroupChatInvitations"
     :on-success #(re-frame/dispatch [::initialize-invitations %])}]})

(rf/defn initialize-communities-enabled
  [cofx]
  {::initialize-communities-enabled nil})

(rf/defn initialize-transactions-management-enabled
  [cofx]
  {::initialize-transactions-management-enabled nil})

(rf/defn initialize-wallet-connect
  [cofx]
  {::initialize-wallet-connect nil})

(rf/defn get-node-config-callback
  {:events [::get-node-config-callback]}
  [{:keys [db] :as cofx} node-config-json]
  (let [node-config (types/json->clj node-config-json)]
    {:db (assoc-in db
          [:multiaccount :wakuv2-config]
          (get node-config :WakuV2Config))}))

(rf/defn get-node-config
  [_]
  (status/get-node-config #(re-frame/dispatch [::get-node-config-callback %])))

(rf/defn get-settings-callback
  {:events [::get-settings-callback]}
  [{:keys [db] :as cofx} settings]
  (let [{:networks/keys [current-network networks]
         :as            settings}
        (data-store.settings/rpc->settings settings)
        multiaccount (dissoc settings :networks/current-network :networks/networks)
        ;;for some reason we save default networks in db, in case when we want to modify default-networks
        ;;for
        ;; existing accounts we have to merge them again into networks
        merged-networks (merge networks config/default-networks-by-id)]
    (rf/merge cofx
              {:db (-> db
                       (dissoc :multiaccounts/login)
                       (assoc :networks/current-network current-network
                              :networks/networks        merged-networks
                              :multiaccount             multiaccount))}
              (data-store.chats/fetch-chats-rpc
               {:on-success
                #(do (re-frame/dispatch [:chats-list/load-success %])
                     (re-frame/dispatch [::get-chats-callback]))})
              (initialize-appearance)
              (initialize-communities-enabled)
              (initialize-wallet-connect)
              (get-node-config)
              (communities/fetch)
              (logging/set-log-level (:log-level multiaccount))
              (activity-center/notifications-fetch-unread-contact-requests)
              (activity-center/notifications-fetch-unread-count))))

(re-frame/reg-fx
 ::open-last-chat
 (fn [key-uid]
   (async-storage/get-item
    :chat-id
    (fn [chat-id]
      (when chat-id
        (async-storage/get-item
         :key-uid
         (fn [stored-key-uid]
           (when (= stored-key-uid key-uid)
             (re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])))))))))

(rf/defn check-last-chat
  {:events [::check-last-chat]}
  [{:keys [db]}]
  {::open-last-chat (get-in db [:multiaccount :key-uid])})

(rf/defn update-wallet-accounts
  [{:keys [db]} accounts]
  (let [existing-accounts (into {} (map #(vector (:address %1) %1) (:multiaccount/accounts db)))
        reduce-fn         (fn [existing-accs new-acc]
                            (let [address (:address new-acc)]
                              (if (:removed new-acc)
                                (dissoc existing-accs address)
                                (assoc existing-accs address new-acc))))
        new-accounts      (reduce reduce-fn existing-accounts (rpc->accounts accounts))]
    {:db (assoc db :multiaccount/accounts (vals new-accounts))}))

(rf/defn get-chats-callback
  {:events [::get-chats-callback]}
  [{:keys [db] :as cofx}]
  (let [{:networks/keys [current-network networks]} db
        notifications-enabled?                      (get-in db [:multiaccount :notifications-enabled?])
        network-id                                  (str (get-in networks
                                                                 [current-network :config :NetworkId]))
        remote-push-notifications-enabled?
        (get-in db [:multiaccount :remote-push-notifications-enabled?])]
    (rf/merge cofx
              (cond-> {::eip1559/check-eip1559-activation
                       {:network-id  network-id
                        :on-enabled  #(log/info "eip1550 is activated")
                        :on-disabled #(log/info "eip1559 is not activated")}
                       ::initialize-wallet
                       [network-id
                        (fn [accounts tokens custom-tokens favourites]
                          (re-frame/dispatch [::initialize-wallet
                                              accounts tokens custom-tokens favourites]))]
                       ::open-last-chat                   (get-in db [:multiaccount :key-uid])}
                (or notifications-enabled? remote-push-notifications-enabled?)
                (assoc ::notifications/enable remote-push-notifications-enabled?))
              (transport/start-messenger)
              (initialize-transactions-management-enabled)
              (check-network-version network-id)
              (contact/initialize-contacts)
              (initialize-browser)
              (mobile-network/on-network-status-change)
              (get-group-chat-invitations)
              (multiaccounts/get-profile-picture)
              (multiaccounts/switch-preview-privacy-mode-flag)
              (link-preview/request-link-preview-whitelist)
              (visibility-status-updates-store/fetch-visibility-status-updates-rpc)
              (switcher-cards-store/fetch-switcher-cards-rpc))))

(defn get-new-auth-method
  [auth-method save-password?]
  (when save-password?
    (when-not (or (= keychain/auth-method-biometric auth-method)
                  (= keychain/auth-method-password auth-method))
      (if (= auth-method keychain/auth-method-biometric-prepare)
        keychain/auth-method-biometric
        keychain/auth-method-password))))

(defn redirect-to-root
  "Decides which root should be initialised depending on user and app state"
  [db]
  (if (get db :tos/accepted?)
    (re-frame/dispatch [:init-root (if config/new-ui-enabled? :shell-stack :chat-stack)])
    (re-frame/dispatch [:init-root :tos])))

(rf/defn login-only-events
  [{:keys [db] :as cofx} key-uid password save-password?]
  (let [auth-method     (:auth-method db)
        new-auth-method (get-new-auth-method auth-method save-password?)]
    (log/debug "[login] login-only-events"
               "auth-method"     auth-method
               "new-auth-method" new-auth-method)
    (rf/merge cofx
              {:db            (assoc db :chats/loading? true)
               :json-rpc/call
               [{:method     "settings_getSettings"
                 :on-success #(do (re-frame/dispatch [::get-settings-callback %])
                                  (redirect-to-root db))}]}
              (notifications/load-notification-preferences)
              (when save-password?
                (keychain/save-user-password key-uid password))
              (keychain/save-auth-method key-uid
                                         (or new-auth-method auth-method keychain/auth-method-none)))))

(rf/defn create-only-events
  [{:keys [db] :as cofx} recovered-account?]
  (let [{:keys [multiaccount
                :multiaccounts/multiaccounts
                :multiaccount/accounts]}
        db
        {:keys [creating?]} (:multiaccounts/login db)
        first-account? (and creating?
                            (empty? multiaccounts))
        tos-accepted? (get db :tos/accepted?)
        {:networks/keys [current-network networks]} db
        network-id (str (get-in networks [current-network :config :NetworkId]))]
    (rf/merge cofx
              {:db          (-> db
                                (dissoc :multiaccounts/login)
                                (assoc :tos/next-root :onboarding-notification :chats/loading? false)
                                (assoc-in [:multiaccount :multiaccounts/first-account] first-account?))
               ::get-tokens [network-id accounts recovered-account?]}
              (finish-keycard-setup)
              (transport/start-messenger)
              (communities/fetch)
              (data-store.chats/fetch-chats-rpc
               {:on-success #(re-frame/dispatch [:chats-list/load-success %])})
              (initialize-communities-enabled)
              (multiaccounts/switch-preview-privacy-mode-flag)
              (link-preview/request-link-preview-whitelist)
              (logging/set-log-level (:log-level multiaccount))

              (if config/new-ui-enabled?
                (navigation/init-root :shell-stack)
                ;; if it's a first account, the ToS will be accepted at welcome carousel
                ;; if not a first account, the ToS might have been accepted by other account logins
                (if (or first-account? tos-accepted?)
                  (navigation/init-root :onboarding-notification)
                  (navigation/init-root :tos))))))

(defn- keycard-setup?
  [cofx]
  (boolean (get-in cofx [:db :keycard :flow])))

(defn on-login-update-db
  [db login-only? now]
  (-> db
      (dissoc :connectivity/ui-status-properties)
      (update :keycard dissoc :from-key-storage-and-migration?)
      (update :keycard      dissoc
              :on-card-read
              :card-read-in-progress?
              :pin
              :multiaccount)
      (assoc :tos-accept-next-root
             (if login-only?
               :chat-stack
               :onboarding-notification))
      (assoc :logged-in-since now)
      (assoc :view-id :home)))

(rf/defn multiaccount-login-success
  [{:keys [db now] :as cofx}]
  (let [{:keys [key-uid password save-password? creating?]}
        (:multiaccounts/login db)

        multiaccounts                                       (:multiaccounts/multiaccounts db)
        recovered-account?                                  (get db :recovered-account?)
        login-only?                                         (not (or creating?
                                                                     recovered-account?
                                                                     (keycard-setup? cofx)))
        from-migration?                                     (get-in db
                                                                    [:keycard
                                                                     :from-key-storage-and-migration?])
        nodes                                               nil]
    (log/debug "[multiaccount] multiaccount-login-success"
               "login-only?"        login-only?
               "recovered-account?" recovered-account?)
    (rf/merge cofx
              {:db            (on-login-update-db db login-only? now)
               :json-rpc/call
               [{:method     "web3_clientVersion"
                 :on-success #(re-frame/dispatch [::initialize-web3-client-version %])}]}
              ;;FIXME
              (when nodes
                (fleet/set-nodes :eth.contract nodes))
              (when (and (not login-only?)
                         (not recovered-account?))
                (wallet/set-initial-blocks-range))
              (when from-migration?
                (utils/show-popup (i18n/label :t/migration-successful)
                                  (i18n/label :t/migration-successful-text)))
              (if login-only?
                (login-only-events key-uid password save-password?)
                (create-only-events recovered-account?)))))

;; FIXME(Ferossgp): We should not copy keys as we denormalize the database,
;; this create desync between actual accounts and the one on login causing broken state
;; UPDATE(cammellos): This code is copying over some fields explicitly as some values
;; are alreayd in `multiaccounts/login` and should not be overriden, as they come from
;; the keychain (save-password), this is not very explicit and we should probably
;; make it clearer
(rf/defn open-login
  [{:keys [db]} override-multiaccount]
  {:db (-> db
           (update :multiaccounts/login
                   merge
                   override-multiaccount)
           (update :multiaccounts/login
                   dissoc
                   :error
                   :password))})

(rf/defn open-login-callback
  {:events [:multiaccounts.login.callback/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (let [key-uid           (get-in db [:multiaccounts/login :key-uid])
        keycard-account?  (boolean (get-in db
                                           [:multiaccounts/multiaccounts
                                            key-uid
                                            :keycard-pairing]))
        goto-key-storage? (:goto-key-storage? db)]
    (if password
      (rf/merge
       cofx
       {:db           (update-in db
                                 [:multiaccounts/login]
                                 assoc
                                 :password       password
                                 :save-password? true)
        :init-root-fx :progress}
       login)
      (rf/merge
       cofx
       {:db (dissoc db :goto-key-storage?)}
       (when keycard-account?
         {:db (-> db
                  (assoc-in [:keycard :pin :status] nil)
                  (assoc-in [:keycard :pin :login] []))})
       #(if keycard-account?
          {:init-root-with-component-fx [:multiaccounts-keycard :multiaccounts]}
          {:init-root-fx :multiaccounts})
       #(when goto-key-storage?
          (navigation/navigate-to-cofx % :actions-not-logged-in nil))))))

(rf/defn get-credentials
  [{:keys [db] :as cofx} key-uid]
  (let [keycard-multiaccount? (boolean (get-in db
                                               [:multiaccounts/multiaccounts key-uid :keycard-pairing]))]
    (log/debug "[login] get-credentials"
               "keycard-multiacc?"
               keycard-multiaccount?)
    (if keycard-multiaccount?
      (keychain/get-keycard-keys cofx key-uid)
      (keychain/get-user-password cofx key-uid))))

(rf/defn get-auth-method-success
  "Auth method: nil - not supported, \"none\" - not selected, \"password\", \"biometric\", \"biometric-prepare\""
  {:events [:multiaccounts.login/get-auth-method-success]}
  [{:keys [db] :as cofx} auth-method]
  (let [key-uid               (get-in db [:multiaccounts/login :key-uid])
        keycard-multiaccount? (boolean (get-in db
                                               [:multiaccounts/multiaccounts key-uid :keycard-pairing]))]
    (log/debug "[login] get-auth-method-success"
               "auth-method"       auth-method
               "keycard-multiacc?" keycard-multiaccount?)
    (rf/merge
     cofx
     {:db (assoc db :auth-method auth-method)}
     #(cond
        (= auth-method keychain/auth-method-biometric)
        (biometric/biometric-auth %)
        (= auth-method keychain/auth-method-password)
        (get-credentials % key-uid)
        (and keycard-multiaccount?
             (get-in db [:keycard :card-connected?]))
        (keycard.common/get-application-info % nil))
     (open-login-callback nil))))

(rf/defn biometric-auth-done
  {:events [:biometric-auth-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (let [key-uid     (get-in db [:multiaccounts/login :key-uid])
        auth-method (get db :auth-method)]
    (log/debug "[biometric] biometric-auth-done"
               "bioauth-success" bioauth-success
               "bioauth-message" bioauth-message
               "bioauth-code"    bioauth-code)
    (if bioauth-success
      (get-credentials cofx key-uid)
      (rf/merge cofx
                {:db (assoc-in db
                      [:multiaccounts/login :save-password?]
                      (= auth-method keychain/auth-method-biometric))}
                (when-not (= auth-method keychain/auth-method-biometric)
                  (keychain/save-auth-method key-uid keychain/auth-method-none))
                (biometric/show-message bioauth-message bioauth-code)
                (open-login-callback nil)))))

(rf/defn save-password
  {:events [:multiaccounts/save-password]}
  [{:keys [db] :as cofx} save-password?]
  (let [bioauth-supported?   (boolean (get db :supported-biometric-auth))
        previous-auth-method (get db :auth-method)]
    (log/debug "[login] save-password"
               "save-password?"       save-password?
               "bioauth-supported?"   bioauth-supported?
               "previous-auth-method" previous-auth-method)
    (rf/merge
     cofx
     {:db (cond-> db
            (not= previous-auth-method
                  keychain/auth-method-biometric-prepare)
            (assoc :auth-method keychain/auth-method-none)
            (or save-password?
                (not bioauth-supported?)
                (and (not save-password?)
                     bioauth-supported?
                     (= previous-auth-method keychain/auth-method-none)))
            (assoc-in [:multiaccounts/login :save-password?] save-password?))}
     (when bioauth-supported?
       (if save-password?
         (popover/show-popover {:view :secure-with-biometric})
         (when-not (= previous-auth-method keychain/auth-method-none)
           (popover/show-popover {:view :disable-password-saving})))))))

(rf/defn welcome-lets-go
  {:events [:welcome-lets-go]}
  [_]
  {:init-root-fx :chat-stack})

(rf/defn multiaccount-selected
  {:events [:multiaccounts.login.ui/multiaccount-selected]}
  [{:keys [db] :as cofx} key-uid]
  ;; We specifically pass a bunch of fields instead of the whole multiaccount
  ;; as we want store some fields in multiaccount that are not here
  (let [multiaccount          (get-in db [:multiaccounts/multiaccounts key-uid])
        keycard-multiaccount? (boolean (:keycard-pairing multiaccount))]
    (rf/merge
     cofx
     {:db             (update db :keycard dissoc :application-info)
      :navigate-to-fx (if keycard-multiaccount? :keycard-login-pin :login)}
     (open-login (select-keys multiaccount [:key-uid :name :public-key :identicon :images])))))

(rf/defn hide-keycard-banner
  {:events [:hide-keycard-banner]}
  [{:keys [db]}]
  {:db                  (assoc db :keycard/banner-hidden true)
   ::async-storage/set! {:keycard-banner-hidden true}})

(rf/defn get-keycard-banner-preference-cb
  {:events [:get-keycard-banner-preference-cb]}
  [{:keys [db]} {:keys [keycard-banner-hidden]}]
  {:db (assoc db :keycard/banner-hidden keycard-banner-hidden)})

(rf/defn get-keycard-banner-preference
  {:events [:get-keycard-banner-preference]}
  [_]
  {::async-storage/get {:keys [:keycard-banner-hidden]
                        :cb   #(re-frame/dispatch [:get-keycard-banner-preference-cb %])}})

(rf/defn get-opted-in-to-new-terms-of-service-cb
  {:events [:get-opted-in-to-new-terms-of-service-cb]}
  [{:keys [db]} {:keys [new-terms-of-service-accepted]}]
  {:db (assoc db :tos/accepted? new-terms-of-service-accepted)})

(rf/defn get-opted-in-to-new-terms-of-service
  "New TOS sprint https://github.com/status-im/status-mobile/pull/12240"
  {:events [:get-opted-in-to-new-terms-of-service]}
  [{:keys [db]}]
  {::async-storage/get {:keys [:new-terms-of-service-accepted]
                        :cb   #(re-frame/dispatch [:get-opted-in-to-new-terms-of-service-cb %])}})

(rf/defn hide-terms-of-services-opt-in-screen
  {:events [:hide-terms-of-services-opt-in-screen]}
  [{:keys [db]}]
  {::async-storage/set! {:new-terms-of-service-accepted true}
   :db                  (assoc db :tos/accepted? true)})
