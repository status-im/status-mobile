(ns status-im.contexts.wallet.events
  (:require
    [clojure.string :as string]
    [react-native.background-timer :as background-timer]
    [react-native.platform :as platform]
    [status-im.contexts.wallet.accounts.add-account.address-to-watch.events]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.data-store :as data-store]
    [status-im.contexts.wallet.events.collectibles]
    [status-im.contexts.wallet.item-types :as item-types]
    [taoensso.timbre :as log]
    [utils.collection]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.i18n :as i18n]
    [utils.number]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-event-fx :wallet/show-account-created-toast
 (fn [{:keys [db]} [address]]
   (let [account (get-in db [:wallet :accounts address])]
     {:db (update db :wallet dissoc :navigate-to-account :new-account?)
      :fx [[:dispatch
            [:toasts/upsert
             {:id   :new-wallet-account-created
              :type :positive
              :text (i18n/label :t/account-created {:name (:name account)})}]]]})))

(rf/reg-event-fx :wallet/navigate-to-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)
    :fx [[:dispatch [:navigate-to :screen/wallet.accounts address]]]}))

(rf/reg-event-fx :wallet/navigate-to-new-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:navigate-to :screen/wallet.accounts address]]
         [:dispatch [:wallet/show-account-created-toast address]]]}))

(rf/reg-event-fx :wallet/switch-current-viewing-account
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :current-viewing-account-address] address)}))

(rf/reg-event-fx :wallet/close-account-page
 (fn [{:keys [db]}]
   {:db (update db :wallet dissoc :current-viewing-account-address)
    :fx [[:dispatch [:pop-to-root :shell-stack]]]}))

(rf/reg-event-fx
 :wallet/get-accounts-success
 (fn [{:keys [db]} [accounts]]
   (let [wallet-accounts     (filter #(not (:chat %)) accounts)
         wallet-db           (get db :wallet)
         new-account?        (:new-account? wallet-db)
         navigate-to-account (:navigate-to-account wallet-db)]
     {:db (assoc-in db
           [:wallet :accounts]
           (utils.collection/index-by :address (data-store/rpc->accounts wallet-accounts)))
      :fx [[:dispatch [:wallet/get-wallet-token]]
           [:dispatch [:wallet/request-collectibles-for-all-accounts {:new-request? true}]]
           (when new-account?
             [:dispatch [:wallet/navigate-to-new-account navigate-to-account]])]})))

(rf/reg-event-fx
 :wallet/get-accounts
 (fn [_]
   {:fx [[:json-rpc/call
          [{:method     "accounts_getAccounts"
            :on-success [:wallet/get-accounts-success]
            :on-error   #(log/info "failed to get accounts "
                                   {:error %
                                    :event :wallet/get-accounts})}]]]}))

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
            :on-error   #(log/info "failed to save account "
                                   {:error %
                                    :event :wallet/save-account})}]]]}))

(rf/reg-event-fx
 :wallet/show-account-deleted-toast
 (fn [_ [toast-message]]
   {:fx [[:dispatch [:toasts/upsert {:type :positive :text toast-message}]]]}))

(rf/reg-event-fx
 :wallet/remove-account-success
 (fn [_ [toast-message _]]
   {:fx [[:dispatch [:wallet/get-accounts]]
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
            :on-error   #(log/info "failed to remove account "
                                   {:error %
                                    :event :wallet/remove-account})}]]]}))

(rf/reg-event-fx
 :wallet/get-wallet-token
 (fn [{:keys [db]}]
   (let [addresses (->> (get-in db [:wallet :accounts])
                        vals
                        (map :address))]
     {:db (assoc-in db [:wallet :ui :tokens-loading?] true)
      :fx [[:json-rpc/call
            [{:method     "wallet_getWalletToken"
              :params     [addresses]
              :on-success [:wallet/store-wallet-token]
              :on-error   [:wallet/get-wallet-token-failed addresses]}]]]})))

(rf/reg-event-fx
 :wallet/get-wallet-token-failed
 (fn [{:keys [db]} [params error]]
   (log/info "failed to get wallet token "
             {:error  error
              :event  :wallet/get-wallet-token
              :params params})
   {:db (assoc-in db [:wallet :ui :tokens-loading?] false)}))

(rf/reg-event-fx
 :wallet/store-wallet-token
 (fn [{:keys [db]} [raw-tokens-data]]
   (let [tokens     (data-store/rpc->tokens raw-tokens-data)
         add-tokens (fn [stored-accounts tokens-per-account]
                      (reduce-kv (fn [accounts address tokens-data]
                                   (if (accounts address)
                                     (update accounts address assoc :tokens tokens-data)
                                     accounts))
                                 stored-accounts
                                 tokens-per-account))]
     {:db (-> db
              (update-in [:wallet :accounts] add-tokens tokens)
              (assoc-in [:wallet :ui :tokens-loading?] false))})))

(rf/defn scan-address-success
  {:events [:wallet/scan-address-success]}
  [{:keys [db]} address]
  {:db (assoc-in db [:wallet :ui :scanned-address] address)})

(rf/defn clean-scanned-address
  {:events [:wallet/clean-scanned-address]}
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui] dissoc :scanned-address)})

(rf/reg-event-fx :wallet/create-derived-addresses
 (fn [{:keys [db]} [{:keys [sha3-pwd path]} on-success]]
   (let [{:keys [address]} (:profile/profile db)]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getDerivedAddresses"
              :params     [sha3-pwd address [path]]
              :on-success on-success
              :on-error   #(log/info "failed to derive address " %)}]]]})))

(rf/reg-event-fx :wallet/add-account-success
 (fn [{:keys [db]} [address]]
   {:db (update db
                :wallet              assoc
                :navigate-to-account address
                :new-account?        true)
    :fx [[:dispatch [:wallet/get-accounts]]
         [:dispatch [:wallet/clear-new-keypair]]]}))

(rf/reg-event-fx :wallet/add-account
 (fn [{:keys [db]}
      [{:keys [sha3-pwd emoji account-name color type] :or {type :generated}}
       {:keys [public-key address path]}]]
   (let [lowercase-address (if address (string/lower-case address) address)
         key-uid           (get-in db [:wallet :ui :create-account :selected-keypair-uid])
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
              :params     [(when (= type :generated) sha3-pwd) account-config]
              :on-success [:wallet/add-account-success lowercase-address]
              :on-error   #(log/info "failed to create account " %)}]]]})))

(rf/reg-event-fx
 :wallet/derive-address-and-add-account
 (fn [_ [account-details]]
   (let [on-success (fn [derived-address-details]
                      (rf/dispatch [:wallet/add-account account-details
                                    (first derived-address-details)]))]
     {:fx [[:dispatch [:wallet/create-derived-addresses account-details on-success]]]})))

(defn add-keypair-and-create-account
  [_ [{:keys [sha3-pwd new-keypair]}]]
  (let [lowercase-address (if (:address new-keypair)
                            (string/lower-case (:address new-keypair))
                            (:address new-keypair))]
    {:fx [[:json-rpc/call
           [{:method     "accounts_addKeypair"
             :params     [sha3-pwd new-keypair]
             :on-success [:wallet/add-account-success lowercase-address]
             :on-error   #(log/info "failed to create keypair " %)}]]]}))

(rf/reg-event-fx :wallet/add-keypair-and-create-account add-keypair-and-create-account)

(defn get-keypairs
  [_]
  {:fx [[:json-rpc/call
         [{:method     "accounts_getKeypairs"
           :params     []
           :on-success [:wallet/get-keypairs-success]
           :on-error   #(log/info "failed to get keypairs " %)}]]]})

(rf/reg-event-fx :wallet/get-keypairs get-keypairs)

(rf/reg-event-fx :wallet/bridge-select-token
 (fn [{:keys [db]} [{:keys [token stack-id]}]]
   (let [to-address (get-in db [:wallet :current-viewing-account-address])]
     {:db (-> db
              (assoc-in [:wallet :ui :send :token] token)
              (assoc-in [:wallet :ui :send :to-address] to-address))
      :fx [[:dispatch [:navigate-to-within-stack [:screen/wallet.bridge-to stack-id]]]]})))

(rf/reg-event-fx :wallet/start-bridge
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :send :tx-type] :bridge)
    :fx [[:dispatch [:open-modal :screen/wallet.bridge]]]}))

(rf/reg-event-fx :wallet/select-bridge-network
 (fn [{:keys [db]} [{:keys [network-chain-id stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :bridge-to-chain-id] network-chain-id)
    :fx [[:dispatch [:navigate-to-within-stack [:screen/wallet.bridge-send stack-id]]]]}))

(rf/reg-event-fx
 :wallet/get-ethereum-chains
 (fn [_]
   {:json-rpc/call
    [{:method     "wallet_getEthereumChains"
      :params     []
      :on-success [:wallet/get-ethereum-chains-success]
      :on-error   #(log/info "failed to get networks " %)}]}))

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
     {:db (assoc-in db [:wallet :networks] network-data)})))

(rf/reg-event-fx :wallet/find-ens
 (fn [{:keys [db]} [input contacts chain-id cb]]
   (let [result (if (empty? input)
                  []
                  (filter #(string/starts-with? (or (:ens-name %) "") input) contacts))]
     (if (and input (empty? result))
       (rf/dispatch [:wallet/search-ens input chain-id cb ".stateofus.eth"])
       {:db (-> db
                (assoc-in [:wallet :ui :search-address :local-suggestions]
                          (map #(assoc % :type item-types/saved-address) result))
                (assoc-in [:wallet :ui :search-address :valid-ens-or-address?]
                          (not-empty result)))}))))

(rf/reg-event-fx :wallet/search-ens
 (fn [_ [input chain-id cb domain]]
   (let [ens (if (string/includes? input ".") input (str input domain))]
     {:fx [[:json-rpc/call
            [{:method     "ens_addressOf"
              :params     [chain-id ens]
              :on-success #(rf/dispatch [:wallet/set-ens-address % ens])
              :on-error   (fn []
                            (if (= domain ".stateofus.eth")
                              (rf/dispatch [:wallet/search-ens input chain-id cb ".eth"])
                              (do
                                (rf/dispatch [:wallet/set-ens-address nil ens])
                                (cb))))}]]]})))

(rf/reg-event-fx :wallet/set-ens-address
 (fn [{:keys [db]} [result ens]]
   {:db
    (-> db
        (assoc-in [:wallet :ui :search-address :local-suggestions]
                  (if result
                    [{:type     item-types/address
                      :ens      ens
                      :address  (eip55/address->checksum result)
                      :networks [:ethereum :optimism]}]
                    []))
        (assoc-in [:wallet :ui :search-address :valid-ens-or-address?]
                  (boolean result)))}))

(rf/reg-event-fx :wallet/fetch-address-suggestions
 (fn [{:keys [db]} [_address]]
   {:db (-> db
            (assoc-in [:wallet :ui :search-address :local-suggestions] nil)
            (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] false))}))

(rf/reg-event-fx :wallet/ens-validation-success
 (fn [{:keys [db]} [_ens]]
   {:db (-> db
            (assoc-in [:wallet :ui :search-address :local-suggestions] nil)
            (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] true))}))

(rf/reg-event-fx :wallet/address-validation-success
 (fn [{:keys [db]} [_]]
   {:db (assoc-in db [:wallet :ui :search-address :valid-ens-or-address?] true)}))

(rf/reg-event-fx :wallet/validate-address
 (fn [{:keys [db]} [address]]
   (let [current-timeout (get-in db [:wallet :ui :search-address :search-timeout])
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/address-validation-success address])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (-> db
              (assoc-in [:wallet :ui :search-address :search-timeout] timeout)
              (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] false))})))

(rf/reg-event-fx :wallet/validate-ens
 (fn [{:keys [db]} [ens]]
   (let [current-timeout (get-in db [:wallet :ui :search-address :search-timeout])
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/ens-validation-success ens])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (-> db
              (assoc-in [:wallet :ui :search-address :search-timeout] timeout)
              (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] false))})))

(rf/reg-event-fx :wallet/clean-local-suggestions
 (fn [{:keys [db]}]
   (let [current-timeout (get-in db [:wallet :ui :search-address :search-timeout])]
     (background-timer/clear-timeout current-timeout)
     {:db (-> db
              (assoc-in [:wallet :ui :search-address :local-suggestions] [])
              (assoc-in [:wallet :ui :search-address :valid-ens-or-address?] false))})))

(rf/reg-event-fx :wallet/clean-ens-or-address-validation
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :search-address :valid-ens-or-address?] false)}))

(rf/reg-event-fx
 :wallet/navigate-to-chain-explorer-from-bottom-sheet
 (fn [_ [explorer-link address]]
   {:fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:browser.ui/open-url (str explorer-link "/" address)]]]}))

(rf/reg-event-fx :wallet/reload
 (fn [_]
   {:fx [[:dispatch-n [[:wallet/get-wallet-token]]]]}))

(rf/reg-event-fx :wallet/start-wallet
 (fn [_]
   {:fx [[:json-rpc/call
          [{:method   "wallet_startWallet"
            :on-error #(log/info "failed to start wallet"
                                 {:error %
                                  :event :wallet/start-wallet})}]]]}))

(rf/reg-event-fx :wallet/initialize
 (fn []
   {:fx [[:dispatch [:wallet/start-wallet]]
         [:dispatch [:wallet/get-ethereum-chains]]
         [:dispatch [:wallet/get-accounts]]
         [:dispatch [:wallet/get-keypairs]]]}))

(rf/reg-event-fx :wallet/share-account
 (fn [_ [{:keys [content title]}]]
   {:fx [[:effects.share/open
          (if platform/ios?
            {:activityItemSources
             [{:placeholderItem {:type    "text"
                                 :content content}
               :item            {:default {:type    "text"
                                           :content content}}
               :linkMetadata    {:title title}}]}
            {:title   title
             :subject title
             :message content})]]}))

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
         chain-ids-by-mode       (utils/get-default-chain-ids-by-mode
                                  {:test-networks-enabled? test-networks-enabled?
                                   :is-goerli-enabled?     is-goerli-enabled?})
         chains-filtered-by-mode (remove #(not (contains? chain-ids-by-mode %)) down-chain-ids)
         chains-down?            (seq chains-filtered-by-mode)
         chain-names             (when chains-down?
                                   (->> (map #(-> (utils/id->network %)
                                                  name
                                                  string/capitalize)
                                             chains-filtered-by-mode)
                                        distinct
                                        (string/join ", ")))]
     (when (seq down-chain-ids)
       (log/info "[wallet] Chain(s) down: " down-chain-ids)
       (log/info "[wallet] Test network enabled: " (boolean test-networks-enabled?))
       (log/info "[wallet] Goerli network enabled: " (boolean is-goerli-enabled?)))
     {:db (assoc-in db [:wallet :statuses :blockchains] chains)
      :fx (when chains-down?
            [[:dispatch
              [:toasts/upsert
               {:id       :chains-down
                :type     :negative
                :text     (i18n/label :t/provider-is-down {:chains chain-names})
                :duration 10000}]]])})))
