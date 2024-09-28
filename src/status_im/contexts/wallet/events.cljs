(ns status-im.contexts.wallet.events
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.set]
    [clojure.string :as string]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    [status-im.contexts.network.data-store :as network.data-store]
    [status-im.contexts.settings.wallet.effects]
    [status-im.contexts.settings.wallet.events]
    [status-im.contexts.wallet.common.activity-tab.events]
    [status-im.contexts.wallet.common.utils.external-links :as external-links]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.data-store :as data-store]
    [status-im.contexts.wallet.db :as db]
    [status-im.contexts.wallet.item-types :as item-types]
    [status-im.contexts.wallet.tokens.events]
    [status-im.feature-flags :as ff]
    [taoensso.timbre :as log]
    [utils.collection]
    [utils.ethereum.chain :as chain]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.i18n :as i18n]
    [utils.number]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(rf/reg-event-fx :wallet/show-account-created-toast
 (fn [{:keys [db]} [address]]
   (let [account-name (get-in db [:wallet :accounts address :name])]
     {:db (update db :wallet dissoc :navigate-to-account :new-account?)
      :fx [[:dispatch
            [:toasts/upsert
             {:id   :new-wallet-account-created
              :type :positive
              :text (i18n/label :t/account-created {:name account-name})}]]]})))

(rf/reg-event-fx :wallet/navigate-to-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)
    :fx [[:dispatch [:navigate-to :screen/wallet.accounts address]]
         [:dispatch [:wallet/fetch-activities-for-current-account]]]}))

(rf/reg-event-fx :wallet/navigate-to-account-within-stack
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)
    :fx [[:dispatch [:navigate-to-within-stack [:screen/wallet.accounts :shell-stack] address]]
         [:dispatch [:wallet/fetch-activities-for-current-account]]]}))

(rf/reg-event-fx :wallet/navigate-to-new-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:navigate-to :screen/wallet.accounts address]]
         [:dispatch [:wallet/show-account-created-toast address]]]}))

(rf/reg-event-fx :wallet/select-account-tab
 (fn [{:keys [db]} [tab]]
   {:db (assoc-in db [:wallet :ui :account-page :active-tab] tab)}))

(rf/reg-event-fx :wallet/clear-account-tab
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :account-page :active-tab] nil)}))

(rf/reg-event-fx :wallet/switch-current-viewing-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)}))

(rf/reg-event-fx :wallet/clean-current-viewing-account
 (fn [{:keys [db]} [ignore-just-completed-transaction?]]
   (let [{:keys [entry-point just-completed-transaction?]} (-> db :wallet :ui :send)
         entry-point-wallet-home?                          (= entry-point :wallet-stack)]
     {:db (cond-> db
            (and (not entry-point)
                 (not ignore-just-completed-transaction?)
                 (not just-completed-transaction?))
            (update :wallet dissoc :current-viewing-account-address)

            entry-point-wallet-home?
            (update-in [:wallet :ui :send] dissoc :entry-point)

            (and entry-point-wallet-home?
                 (not just-completed-transaction?))
            (update :wallet dissoc :current-viewing-account-address))})))

(rf/reg-event-fx :wallet/close-account-page
 (fn [{:keys [db]}]
   (let [just-completed-transaction? (get-in db [:wallet :ui :send :just-completed-transaction?])]
     {:db (update db :wallet dissoc :current-viewing-account-address)
      :fx [(when-not just-completed-transaction?
             [:dispatch [:wallet/clear-account-tab]])]})))

(defn log-rpc-error
  [_ [{:keys [event params]} error]]
  (log/warn (str "[wallet] Failed to " event)
            {:params params
             :error  error}))

(rf/reg-event-fx :wallet/log-rpc-error log-rpc-error)

(def refresh-accounts-fx-dispatches
  [[:dispatch [:wallet/get-wallet-token-for-all-accounts]]
   [:dispatch [:wallet/request-collectibles-for-all-accounts {:new-request? true}]]
   [:dispatch [:wallet/check-recent-history-for-all-accounts]]])

(rf/reg-event-fx
 :wallet/fetch-assets-for-address
 (fn [_ [address]]
   {:fx [[:dispatch [:wallet/get-wallet-token-for-account address]]
         [:dispatch [:wallet/request-collectibles-for-account address]]
         [:dispatch [:wallet/check-recent-history-for-account address]]]}))

(defn- reconcile-accounts
  [db-accounts-by-address new-accounts]
  (reduce
   (fn [res {:keys [address] :as account}]
     ;; Because we add extra fields (tokens and collectibles) into the RPC
     ;; response from accounts_getAccounts, if we are offline we want to keep
     ;; the old balances in the accounts, thus we merge the up-to-date account
     ;; from status-go into the cached accounts. We also merge when online
     ;; because we will re-fetch balances anyway.
     ;;
     ;; Refactor improvement: don't augment entities from status-go, store
     ;; tokens and collectibles in separate keys in the app-db indexed by
     ;; account address.
     (assoc res
            address
            (-> (get db-accounts-by-address address)
                (merge account)
                ;; These should not be cached, otherwise when going
                ;; offline->online collectibles won't be fetched.
                (dissoc :current-collectible-idx :has-more-collectibles?))))
   {}
   new-accounts))

(rf/reg-event-fx :wallet/get-accounts-success
 (fn [{:keys [db]} [accounts]]
   (let [wallet-accounts     (data-store/rpc->accounts accounts)
         wallet-db           (get db :wallet)
         new-account?        (:new-account? wallet-db)
         navigate-to-account (:navigate-to-account wallet-db)]
     {:db (update-in db [:wallet :accounts] reconcile-accounts wallet-accounts)
      :fx (concat (when (or (data-store/tokens-never-loaded? db)
                            (network.data-store/online? db))
                    refresh-accounts-fx-dispatches)
                  [(when new-account?
                     [:dispatch [:wallet/navigate-to-new-account navigate-to-account]])])})))

(rf/reg-event-fx :wallet/get-accounts
 (fn []
   {:fx [[:json-rpc/call
          [{:method     "accounts_getAccounts"
            :on-success [:wallet/get-accounts-success]
            :on-error   [:wallet/log-rpc-error {:event :wallet/get-accounts}]}]]]}))

(rf/reg-event-fx
 :wallet/save-account
 (fn [_ [{:keys [account on-success]}]]
   {:fx [[:json-rpc/call
          [{:method     "accounts_saveAccount"
            :params     [(data-store/<-account account)]
            :on-success (fn []
                          (rf/dispatch [:wallet/get-accounts])
                          (when (fn? on-success)
                            (on-success)))
            :on-error   [:wallet/log-rpc-error {:event :wallet/save-account}]}]]]}))

(rf/reg-event-fx
 :wallet/show-account-deleted-toast
 (fn [_ [toast-message]]
   {:fx [[:dispatch [:toasts/upsert {:type :positive :text toast-message}]]]}))

(rf/reg-event-fx
 :wallet/remove-account-success
 (fn [_ [toast-message _]]
   {:fx [[:dispatch [:wallet/clean-current-viewing-account]]
         [:dispatch [:wallet/get-accounts]]
         [:dispatch [:wallet/get-keypairs]]
         [:dispatch-later
          {:ms       100
           :dispatch [:hide-bottom-sheet]}]
         [:dispatch-later
          {:ms       100
           :dispatch [:pop-to-root :shell-stack]}]
         [:dispatch-later
          {:ms       100
           :dispatch [:wallet/show-account-deleted-toast toast-message]}]]}))

(rf/reg-event-fx
 :wallet/remove-account
 (fn [_ [{:keys [address toast-message]}]]
   {:fx [[:json-rpc/call
          [{:method     "accounts_deleteAccount"
            :params     [address]
            :on-success [:wallet/remove-account-success toast-message]
            :on-error   [:wallet/log-rpc-error {:event :wallet/remove-account}]}]]]}))

(rf/reg-event-fx :wallet/get-wallet-token-for-all-accounts
 (fn [{:keys [db]}]
   {:fx (->> (get-in db [:wallet :accounts])
             vals
             (mapv
              (fn [{:keys [address]}]
                [:dispatch [:wallet/get-wallet-token-for-account address]])))}))

(rf/reg-event-fx :wallet/get-wallet-token-for-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :ui :tokens-loading address] true)
    :fx [[:json-rpc/call
          [{:method     "wallet_getWalletToken"
            :params     [[address]]
            :on-success [:wallet/store-wallet-token address]
            :on-error   [:wallet/get-wallet-token-for-account-failed address]}]]]}))

(rf/reg-event-fx
 :wallet/get-wallet-token-for-account-failed
 (fn [{:keys [db]} [address error]]
   (log/info "failed to get wallet token "
             {:error  error
              :event  :wallet/get-wallet-token-for-account
              :params address})
   {:db (assoc-in db [:wallet :ui :tokens-loading address] false)}))

(rf/reg-event-fx
 :wallet/store-wallet-token
 (fn [{:keys [db]} [address raw-tokens-data]]
   (let [tokens     (data-store/rpc->tokens raw-tokens-data)
         add-tokens (fn [stored-accounts tokens-per-account]
                      (reduce-kv (fn [accounts address tokens-data]
                                   (if (contains? accounts address)
                                     (update accounts address assoc :tokens tokens-data)
                                     accounts))
                                 stored-accounts
                                 tokens-per-account))]
     {:db (-> db
              (update-in [:wallet :accounts] add-tokens tokens)
              (assoc-in [:wallet :ui :tokens-loading address] false))})))

(rf/defn scan-address-success
  {:events [:wallet/scan-address-success]}
  [{:keys [db]} address]
  {:db (assoc-in db [:wallet :ui :scanned-address] address)})

(rf/defn clean-scanned-address
  {:events [:wallet/clean-scanned-address]}
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui] dissoc :scanned-address)})

(rf/reg-event-fx :wallet/add-account-success
 (fn [{:keys [db]} [address]]
   {:db (-> db
            (assoc-in [:wallet :navigate-to-account] address)
            (assoc-in [:wallet :new-account?] true))
    :fx [[:dispatch [:wallet/get-accounts]]
         [:dispatch [:wallet/get-keypairs]]
         [:dispatch [:wallet/clear-create-account]]]}))

(rf/reg-event-fx :wallet/add-account
 (fn [_
      [{:keys [key-uid password account-name emoji color type]
        :or   {type :generated}}
       {:keys [public-key address path] :as _derived-account}]]
   (let [lowercase-address (some-> address
                                   string/lower-case)
         account-config    {:key-uid    (when (= type :generated) key-uid)
                            :wallet     false
                            :chat       false
                            :type       type
                            :name       account-name
                            :emoji      emoji
                            :path       path
                            :address    lowercase-address
                            :public-key public-key
                            :colorID    color}]
     {:fx [[:json-rpc/call
            [{:method     "accounts_addAccount"
              :params     [(when (= type :generated)
                             (security/safe-unmask-data password))
                           account-config]
              :on-success [:wallet/add-account-success lowercase-address]
              :on-error   [:wallet/log-rpc-error
                           {:event  :wallet/add-account
                            :params account-config}]}]]]})))

(defn get-keypairs
  [_]
  {:fx [[:json-rpc/call
         [{:method     "accounts_getKeypairs"
           :params     []
           :on-success [:wallet/get-keypairs-success]
           :on-error   [:wallet/log-rpc-error {:event :wallet/get-keypairs}]}]]]})

(rf/reg-event-fx :wallet/get-keypairs get-keypairs)

(rf/reg-event-fx :wallet/bridge-select-token
 (fn [{:keys [db]} [{:keys [token token-symbol stack-id start-flow?]}]]
   (let [missing-recipient? (-> db :wallet :ui :send :to-address nil?)
         to-address         (-> db :wallet :current-viewing-account-address)]
     {:db (cond-> db
            :always            (assoc-in [:wallet :ui :send :tx-type] :tx/bridge)
            token              (assoc-in [:wallet :ui :send :token] token)
            token-symbol       (assoc-in [:wallet :ui :send :token-symbol] token-symbol)
            missing-recipient? (assoc-in [:wallet :ui :send :to-address] to-address))
      :fx [[:dispatch
            [:wallet/wizard-navigate-forward
             {:current-screen stack-id
              :start-flow?    start-flow?
              :flow-id        :wallet-bridge-flow}]]]})))

(rf/reg-event-fx :wallet/start-bridge
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :send :tx-type] :tx/bridge)
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:start-flow? true
            :flow-id     :wallet-bridge-flow}]]]}))

(rf/reg-event-fx :wallet/select-bridge-network
 (fn [{:keys [db]} [{:keys [network-chain-id stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :bridge-to-chain-id] network-chain-id)
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :flow-id        :wallet-bridge-flow}]]]}))

(rf/reg-event-fx
 :wallet/get-ethereum-chains
 (fn [_]
   {:json-rpc/call
    [{:method     "wallet_getEthereumChains"
      :params     []
      :on-success [:wallet/get-ethereum-chains-success]
      :on-error   [:wallet/log-rpc-error {:event :wallet/get-ethereum-chains}]}]}))

(rf/reg-event-fx
 :wallet/get-ethereum-chains-success
 (fn [{:keys [db]} [data]]
   (let [network-data
         {:test (map #(->> %
                           :Test
                           data-store/rpc->network)
                     data)
          :prod (map #(->> %
                           :Prod
                           data-store/rpc->network)
                     data)}]
     {:fx [(when (ff/enabled? ::ff/wallet.swap)
             [:dispatch [:wallet.tokens/get-token-list]])]
      :db (assoc-in db [:wallet :networks] network-data)})))

(rf/reg-event-fx
 :wallet/find-ens
 (fn [{:keys [db]} [input contacts on-error-fn]]
   (let [result (if (empty? input)
                  []
                  (filter #(string/starts-with? (or (:ens-name %) "") input) contacts))]
     (if (and input (empty? result))
       {:fx [[:dispatch [:wallet/search-ens input on-error-fn ".stateofus.eth"]]]}
       {:db (-> db
                (assoc-in [:wallet :ui :search-address :local-suggestions]
                          (map #(assoc % :type item-types/saved-address) result))
                (assoc-in [:wallet :ui :search-address :valid-ens-or-address?]
                          (not-empty result)))}))))

(rf/reg-event-fx
 :wallet/search-ens
 (fn [{db :db} [input on-error-fn domain]]
   (let [ens      (if (string/includes? input ".")
                    input
                    (str input domain))
         chain-id (network-utils/network->chain-id db :mainnet)]
     {:fx [[:json-rpc/call
            [{:method     "ens_addressOf"
              :params     [chain-id ens]
              :on-success #(rf/dispatch [:wallet/set-ens-address % ens])
              :on-error   (fn []
                            (if (= domain ".stateofus.eth")
                              (rf/dispatch [:wallet/search-ens input on-error-fn ".eth"])
                              (do
                                (rf/dispatch [:wallet/set-ens-address nil ens])
                                (on-error-fn))))}]]]})))

(defn- resolved-address->prefixed-address
  [address networks]
  (let [prefixes (->> networks
                      (map network-utils/network->short-name)
                      network-utils/short-names->network-preference-prefix)]
    (str prefixes (eip55/address->checksum address))))

(rf/reg-event-fx
 :wallet/set-ens-address
 (fn [{:keys [db]} [result ens]]
   (let [networks   [constants/ethereum-network-name constants/optimism-network-name]
         suggestion (if result
                      [{:type         item-types/address
                        :ens          ens
                        :address      (eip55/address->checksum result)
                        :networks     networks
                        :full-address (resolved-address->prefixed-address result networks)}]
                      [])]
     {:db (-> db
              (assoc-in [:wallet :ui :search-address :local-suggestions] suggestion)
              (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] (boolean result)))})))

(rf/reg-event-fx :wallet/address-validation-success
 (fn [{:keys [db]}]
   {:db (update-in db
                   [:wallet :ui :search-address]
                   assoc
                   :valid-ens-or-address? true
                   :loading?              false)}))

(rf/reg-event-fx :wallet/address-validation-failed
 (fn [{:keys [db]}]
   {:db (update-in db
                   [:wallet :ui :search-address]
                   assoc
                   :valid-ens-or-address? false
                   :loading?              false)}))

(rf/reg-event-fx :wallet/clean-local-suggestions
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc-in [:wallet :ui :search-address :local-suggestions] [])
            (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] false))}))

(rf/reg-event-fx :wallet/searching-address
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :search-address :loading?] true)}))

(rf/reg-event-fx
 :wallet/navigate-to-chain-explorer-from-bottom-sheet
 (fn [_ [explorer-link address]]
   {:fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:browser.ui/open-url (str explorer-link "/" address)]]]}))

(rf/reg-event-fx
 :wallet/navigate-to-chain-explorer
 (fn [{:keys [db]} [{:keys [network chain-id address]}]]
   (let [chain-id      (or chain-id (network-utils/network->chain-id db network))
         explorer-link (external-links/get-explorer-url-by-chain-id chain-id)]
     {:fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:browser.ui/open-url (str explorer-link "/" address)]]]})))

(rf/reg-event-fx :wallet/reload
 (fn [_]
   {:fx [[:dispatch [:wallet/get-wallet-token-for-all-accounts]]]}))

(rf/reg-event-fx :wallet/start-wallet
 (fn [_]
   {:fx [[:json-rpc/call
          [{:method   "wallet_startWallet"
            :on-error [:wallet/log-rpc-error {:event :wallet/start-wallet}]}]]]}))

(rf/reg-event-fx :wallet/check-recent-history-for-all-accounts
 (fn [{:keys [db]}]
   {:fx (->> (get-in db [:wallet :accounts])
             vals
             (mapv (fn [{:keys [address]}]
                     [:dispatch [:wallet/check-recent-history-for-account address]])))}))

(rf/reg-event-fx
 :wallet/check-recent-history-for-account
 (fn [{:keys [db]} [address]]
   (let [chain-ids (chain/chain-ids db)
         params    [chain-ids [address]]]
     {:fx [[:json-rpc/call
            [{:method   "wallet_checkRecentHistoryForChainIDs"
              :params   params
              :on-error [:wallet/log-rpc-error
                         {:event  :wallet/check-recent-history-for-account
                          :params params}]}]]]})))

(rf/reg-event-fx :wallet/initialize
 (fn []
   {:fx [[:dispatch [:wallet/start-wallet]]
         [:dispatch [:wallet/get-ethereum-chains]]
         [:dispatch [:wallet/get-accounts]]
         [:dispatch [:wallet/get-keypairs]]
         [:dispatch [:wallet/get-saved-addresses]]]}))

(rf/reg-event-fx :wallet/share-account
 (fn [_ [{:keys [content title]}]]
   {:fx [[:effects.share/open
          {:options (if platform/ios?
                      {:activityItemSources
                       [{:placeholderItem {:type    :text
                                           :content content}
                         :item            {:default {:type    :text
                                                     :content content}}
                         :linkMetadata    {:title title}}]}
                      {:title   title
                       :subject title
                       :message content})}]]}))

(rf/reg-event-fx
 :wallet/blockchain-status-changed
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [chains                  (-> (transforms/json->clj message)
                                     (update-keys (comp utils.number/parse-int name)))
         down-chain-ids          (-> (select-keys chains
                                                  (for [[k v] chains :when (= v "down")] k))
                                     keys)
         test-networks-enabled?  (get-in db [:profile/profile :test-networks-enabled?])
         is-goerli-enabled?      (get-in db [:profile/profile :is-goerli-enabled?])
         chain-ids-by-mode       (network-utils/get-default-chain-ids-by-mode
                                  {:test-networks-enabled? test-networks-enabled?
                                   :is-goerli-enabled?     is-goerli-enabled?})
         chains-filtered-by-mode (remove #(not (contains? chain-ids-by-mode %)) down-chain-ids)
         chains-down?            (and (network.data-store/online? db) (seq chains-filtered-by-mode))
         chain-names             (when chains-down?
                                   (->> (map #(-> (network-utils/id->network %)
                                                  name
                                                  string/capitalize)
                                             chains-filtered-by-mode)
                                        distinct
                                        (string/join ", ")))]
     (when (seq down-chain-ids)
       (log/info "[wallet] Chain(s) down: " down-chain-ids)
       (log/info "[wallet] Chain name(s) down: " chain-names)
       (log/info "[wallet] Test network enabled: " (boolean test-networks-enabled?))
       (log/info "[wallet] Goerli network enabled: " (boolean is-goerli-enabled?)))

     ;; NOTE <shivekkhurana>: We used to show an error toast, but disabled it because the down
     ;; signal is sent randomly. Needs to be investigated and enabled again !
     ;; Context: https://github.com/status-im/status-mobile/issues/21054

     {:db (assoc-in db [:wallet :statuses :blockchains] chains)})))

(rf/reg-event-fx :wallet/reset-selected-networks
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :network-filter] db/network-filter-defaults)}))

(rf/reg-event-fx :wallet/update-selected-networks
 (fn [{:keys [db]} [network-name]]
   (let [selected-networks (get-in db [:wallet :ui :network-filter :selected-networks])
         selector-state    (get-in db [:wallet :ui :network-filter :selector-state])
         contains-network? (contains? selected-networks network-name)
         update-fn         (if contains-network? disj conj)
         networks-count    (count selected-networks)]
     (cond (= selector-state :default)
           {:db (-> db
                    (assoc-in [:wallet :ui :network-filter :selected-networks] #{network-name})
                    (assoc-in [:wallet :ui :network-filter :selector-state] :changed))}

           ;; reset the list
           ;; - if user is removing the last network in the list
           ;; - if all networks is selected
           (or (and (= networks-count 1) contains-network?)
               (and (= (inc networks-count) constants/default-network-count) (not contains-network?)))
           {:fx [[:dispatch [:wallet/reset-selected-networks]]]}

           :else
           {:db
            (update-in db [:wallet :ui :network-filter :selected-networks] update-fn network-name)}))))

(rf/reg-event-fx
 :wallet/get-crypto-on-ramps-success
 (fn [{:keys [db]} [data]]
   (let [crypto-on-ramps (cske/transform-keys transforms/->kebab-case-keyword data)]
     {:db (assoc-in db
           [:wallet :crypto-on-ramps]
           {:one-time  (remove #(string/blank? (:site-url %)) crypto-on-ramps)
            :recurrent (remove #(string/blank? (:recurrent-site-url %)) crypto-on-ramps)})})))

(rf/reg-event-fx
 :wallet/get-crypto-on-ramps
 (fn [_]
   {:fx [[:json-rpc/call
          [{:method     "wallet_getCryptoOnRamps"
            :on-success [:wallet/get-crypto-on-ramps-success]
            :on-error   [:wallet/log-rpc-error {:event :wallet/get-crypto-on-ramps}]}]]]}))

(rf/reg-event-fx
 :wallet/resolve-ens
 (fn [{db :db} [{:keys [ens on-success on-error]}]]
   (let [chain-id (network-utils/network->chain-id db constants/mainnet-network-name)]
     {:fx [[:json-rpc/call
            [{:method     "ens_addressOf"
              :params     [chain-id ens]
              :on-success on-success
              :on-error   on-error}]]]})))

(rf/reg-event-fx
 :wallet/process-keypair-from-backup
 (fn [_ [{:keys [backedUpKeypair]}]]
   {:fx [[:dispatch [:wallet/reconcile-keypairs [backedUpKeypair]]]]}))

(rf/reg-event-fx
 :wallet/process-watch-only-account-from-backup
 (fn [_ [{:keys [backedUpWatchOnlyAccount]}]]
   {:fx [[:dispatch [:wallet/reconcile-watch-only-accounts [backedUpWatchOnlyAccount]]]]}))

(defn reconcile-watch-only-accounts
  [{:keys [db]} [watch-only-accounts]]
  (let [existing-accounts-by-address (get-in db [:wallet :accounts])
        group-label                  #(if % :removed-accounts :updated-accounts)
        {:keys [removed-accounts
                updated-accounts]}   (->> watch-only-accounts
                                          (map data-store/rpc->account)
                                          (group-by (comp group-label :removed)))
        existing-account-addresses   (set (keys existing-accounts-by-address))
        removed-account-addresses    (set (map :address removed-accounts))
        updated-account-addresses    (set (map :address updated-accounts))
        new-account-addresses        (clojure.set/difference updated-account-addresses
                                                             existing-account-addresses)]
    (cond-> {:db (update-in db
                            [:wallet :accounts]
                            (fn [existing-accounts]
                              (merge-with merge
                                          (apply dissoc existing-accounts removed-account-addresses)
                                          (utils.collection/index-by :address updated-accounts))))}

      (seq new-account-addresses)
      (assoc :fx
             (mapv (fn [address] [:dispatch [:wallet/fetch-assets-for-address address]])
                   new-account-addresses)))))

(rf/reg-event-fx :wallet/reconcile-watch-only-accounts reconcile-watch-only-accounts)

(defn reconcile-keypairs
  [{:keys [db]} [keypairs]]
  (let [existing-keypairs-by-id               (get-in db [:wallet :keypairs])
        existing-accounts-by-address          (get-in db [:wallet :accounts])
        {:keys [removed-keypair-ids
                removed-account-addresses
                updated-keypairs-by-id
                updated-accounts-by-address]} (data-store/reconcile-keypairs keypairs)
        updated-keypair-ids                   (set (keys updated-keypairs-by-id))
        updated-account-addresses             (set (keys updated-accounts-by-address))
        existing-account-addresses            (set (keys existing-accounts-by-address))
        new-account-addresses                 (clojure.set/difference updated-account-addresses
                                                                      existing-account-addresses)
        old-account-addresses                 (->> (vals existing-accounts-by-address)
                                                   (filter (fn [{:keys [address key-uid]}]
                                                             (and (contains? updated-keypair-ids key-uid)
                                                                  (not (contains?
                                                                        updated-accounts-by-address
                                                                        address)))))
                                                   (map :address))]
    (cond-> {:db (-> db
                     (assoc-in [:wallet :keypairs]
                               (-> (apply dissoc existing-keypairs-by-id removed-keypair-ids)
                                   (merge updated-keypairs-by-id)))
                     (assoc-in [:wallet :accounts]
                               (merge-with merge
                                           (apply dissoc
                                                  existing-accounts-by-address
                                                  (into removed-account-addresses
                                                        old-account-addresses))
                                           updated-accounts-by-address)))}

      (seq new-account-addresses)
      (assoc :fx
             (mapv (fn [address] [:dispatch [:wallet/fetch-assets-for-address address]])
                   new-account-addresses)))))

(rf/reg-event-fx :wallet/reconcile-keypairs reconcile-keypairs)
