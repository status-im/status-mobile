(ns status-im2.contexts.wallet.events
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :as cske]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.foundations.colors :as colors]
    [react-native.background-timer :as background-timer]
    [status-im2.common.data-store.wallet :as data-store]
    [status-im2.contexts.wallet.temp :as temp]
    [taoensso.timbre :as log]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.number]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as types]))

(rf/reg-event-fx :wallet/show-account-created-toast
 (fn [{:keys [db]} [address]]
   (let [account (get-in db [:wallet :accounts address])]
     {:db (update db :wallet dissoc :navigate-to-account :new-account?)
      :fx [[:dispatch
            [:toasts/upsert
             {:id         :new-wallet-account-created
              :icon       :i/correct
              :icon-color colors/success-50
              :text       (i18n/label :t/account-created {:name name})}]]]})))

(rf/reg-event-fx :wallet/navigate-to-account
 (fn [{:keys [db]} [address]]
   (let [new-account? (get-in db [:wallet :new-account?])]
     (cond-> {:db (assoc-in db [:wallet :current-viewing-account-address] address)
              :fx [[:dispatch [:navigate-to :wallet-accounts address]]]}

       new-account?
       (update :fx conj [:dispatch [:wallet/show-account-created-toast address]])))))

(rf/reg-event-fx :wallet/close-account-page
 (fn [{:keys [db]}]
   {:db (update db :wallet dissoc :current-viewing-account-address)
    :fx [[:dispatch [:navigate-back]]]}))

(rf/reg-event-fx
 :wallet/get-accounts-success
 (fn [{:keys [db]} [accounts]]
   (let [wallet-accounts     (filter #(not (:chat %)) accounts)
         wallet-db           (get db :wallet)
         new-account?        (:new-account? wallet-db)
         navigate-to-account (:navigate-to-account wallet-db)]
     {:db (reduce (fn [db {:keys [address] :as account}]
                    (assoc-in db [:wallet :accounts address] account))
                  db
                  (data-store/rpc->accounts wallet-accounts))
      :fx [[:dispatch [:wallet/get-wallet-token]]
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
 :wallet/get-wallet-token
 (fn [{:keys [db]}]
   (let [addresses (->> (get-in db [:wallet :accounts])
                        vals
                        (map :address))]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getWalletToken"
              :params     [addresses]
              :on-success [:wallet/store-wallet-token]
              :on-error   #(log/info "failed to get wallet token "
                                     {:error  %
                                      :event  :wallet/get-wallet-token
                                      :params addresses})}]]]})))

(defn- fix-chain-id-keys
  [token]
  (update token :balances-per-chain update-keys (comp utils.number/parse-int name)))

(rf/reg-event-fx
 :wallet/store-wallet-token
 (fn [{:keys [db]} [raw-tokens-data]]
   (let [tokens     (-> raw-tokens-data
                        (update-keys name)
                        (update-vals #(cske/transform-keys csk/->kebab-case %))
                        (update-vals #(mapv fix-chain-id-keys %)))
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
  {:db (assoc db :wallet/scanned-address address)})

(rf/defn clean-scanned-address
  {:events [:wallet/clean-scanned-address]}
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui :send] dissoc :to-address)})

(rf/reg-event-fx :wallet/create-derived-addresses
 (fn [{:keys [db]} [password {:keys [path]} on-success]]
   (let [{:keys [wallet-root-address]} (:profile/profile db)
         sha3-pwd                      (native-module/sha3 (str (security/safe-unmask-data password)))]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getDerivedAddresses"
              :params     [sha3-pwd wallet-root-address [path]]
              :on-success on-success
              :on-error   #(log/info "failed to derive address " %)}]]]})))

(rf/reg-event-fx :wallet/add-account-success
 (fn [{:keys [db]} [address]]
   {:db (update db
                :wallet              assoc
                :navigate-to-account address
                :new-account?        true)
    :fx [[:dispatch [:wallet/get-accounts]]]}))

(rf/reg-event-fx :wallet/add-account
 (fn [{:keys [db]}
      [password {:keys [emoji account-name color type] :or {type :generated}}
       {:keys [public-key address path]}]]
   (let [lowercase-address (if address (string/lower-case address) address)
         key-uid           (get-in db [:profile/profile :key-uid])
         sha3-pwd          (native-module/sha3 (security/safe-unmask-data password))
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
 (fn [_ [password account-details]]
   (let [on-success (fn [derived-address-details]
                      (rf/dispatch [:wallet/add-account password account-details
                                    (first derived-address-details)]))]
     {:fx [[:dispatch [:wallet/create-derived-addresses password account-details on-success]]]})))

(rf/defn get-ethereum-chains
  {:events [:wallet/get-ethereum-chains]}
  [{:keys [db]}]
  {:fx [[:json-rpc/call
         [{:method     "wallet_getEthereumChains"
           :params     []
           :on-success [:wallet/get-ethereum-chains-success]
           :on-error   #(log/info "failed to get networks " %)}]]]})

(rf/reg-event-fx
 :wallet/get-ethereum-chains-success
 (fn [{:keys [db]} [data]]
   (let [network-data
         {:test (map #(->> %
                           :Test
                           data-store/<-rpc)
                     data)
          :prod (map #(->> %
                           :Prod
                           data-store/<-rpc)
                     data)}]
     {:db (assoc db :wallet/networks network-data)})))

(def collectibles-request-batch-size 1000)

(defn displayable-collectible?
  [{:keys [image-url animation-url]}]
  (or (not (string/blank? animation-url))
      (not (string/blank? image-url))))

(defn store-collectibles
  [{:keys [db]} [collectibles]]
  (let [stored-collectibles      (get-in db [:wallet :collectibles])
        displayable-collectibles (filter displayable-collectible? collectibles)]
    {:db (assoc-in db
          [:wallet :collectibles]
          (reduce conj displayable-collectibles stored-collectibles))}))

(rf/reg-event-fx :wallet/store-collectibles store-collectibles)

(defn clear-stored-collectibles
  [{:keys [db]}]
  {:db (update db :wallet dissoc :collectibles)})

(rf/reg-event-fx :wallet/clear-stored-collectibles clear-stored-collectibles)

(defn store-last-collectible-details
  [{:keys [db]} [collectible]]
  {:db (assoc-in db
        [:wallet :last-collectible-details]
        collectible)})

(rf/reg-event-fx :wallet/store-last-collectible-details store-last-collectible-details)

(rf/reg-event-fx
 :wallet/request-collectibles
 (fn [{:keys [db]} [{:keys [start-at-index new-request?]}]]
   (let [request-id          0
         collectibles-filter nil
         request-params      [request-id
                              [(chain/chain-id db)]
                              (map :address (:profile/wallet-accounts db))
                              collectibles-filter
                              start-at-index
                              collectibles-request-batch-size]]
     {:json-rpc/call [{:method     "wallet_filterOwnedCollectiblesAsync"
                       :params     request-params
                       :on-success #()
                       :on-error   (fn [error]
                                     (log/error "failed to request collectibles"
                                                {:event  :wallet/request-collectibles
                                                 :error  error
                                                 :params request-params}))}]
      :fx            (if new-request?
                       [[:dispatch [:wallet/clear-stored-collectibles]]]
                       [])})))

(rf/reg-event-fx :wallet/owned-collectibles-filtering-done
 (fn [_ [{:keys [message]}]]
   (let [response                               (cske/transform-keys csk/->kebab-case-keyword
                                                                     (types/json->clj message))
         {:keys [collectibles has-more offset]} response
         start-at-index                         (+ offset (count collectibles))]
     {:fx
      [[:dispatch [:wallet/store-collectibles collectibles]]
       (when has-more
         [:dispatch
          [:wallet/request-collectibles
           {:start-at-index start-at-index}]])]})))

(rf/reg-event-fx :wallet/get-collectible-details
 (fn [_ [collectible-id]]
   (let [request-id               0
         collectible-id-converted (cske/transform-keys csk/->PascalCaseKeyword collectible-id)
         request-params           [request-id [collectible-id-converted]]]
     {:json-rpc/call [{:method   "wallet_getCollectiblesDetailsAsync"
                       :params   request-params
                       :on-error (fn [error]
                                   (log/error "failed to request collectible"
                                              {:event  :wallet/get-collectible-details
                                               :error  error
                                               :params request-params}))}]})))

(rf/reg-event-fx :wallet/get-collectible-details-done
 (fn [_ [{:keys [message]}]]
   (let [response               (cske/transform-keys csk/->kebab-case-keyword
                                                     (types/json->clj message))
         {:keys [collectibles]} response
         collectible            (first collectibles)]
     (if collectible
       {:fx
        [[:dispatch [:wallet/store-last-collectible-details collectible]]]}
       (log/error "failed to get collectible details"
                  {:event    :wallet/get-collectible-details-done
                   :response response})))))

(rf/reg-event-fx :wallet/fetch-address-suggestions
 (fn [{:keys [db]} [address]]
   {:db (assoc db
               :wallet/local-suggestions
               (cond
                 (= address
                    (get-in
                     temp/address-local-suggestion-saved-contact-address-mock
                     [:accounts 0 :address]))
                 [temp/address-local-suggestion-saved-contact-address-mock]
                 (= address
                    (get temp/address-local-suggestion-saved-address-mock
                         :address))
                 [temp/address-local-suggestion-saved-address-mock]
                 :else (temp/find-matching-addresses address))
               :wallet/valid-ens-or-address?
               false)}))

(rf/reg-event-fx :wallet/ens-validation-success
 (fn [{:keys [db]} [ens]]
   {:db (assoc db
               :wallet/local-suggestions     (if (= ens
                                                    (:ens temp/ens-local-suggestion-saved-address-mock))
                                               [temp/ens-local-suggestion-saved-address-mock]
                                               [temp/ens-local-suggestion-mock])
               :wallet/valid-ens-or-address? true)}))

(rf/reg-event-fx :wallet/address-validation-success
 (fn [{:keys [db]} [_]]
   {:db (assoc db :wallet/valid-ens-or-address? true)}))

(rf/reg-event-fx :wallet/validate-address
 (fn [{:keys [db]} [address]]
   (let [current-timeout (get db :wallet/search-timeout)
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/address-validation-success address])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db
                 :wallet/valid-ens-or-address? false
                 :wallet/search-timeout        timeout)})))

(rf/reg-event-fx :wallet/validate-ens
 (fn [{:keys [db]} [ens]]
   (let [current-timeout (get db :wallet/search-timeout)
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/ens-validation-success ens])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db
                 :wallet/valid-ens-or-address? false
                 :wallet/search-timeout        timeout)})))

(rf/reg-event-fx :wallet/clean-local-suggestions
 (fn [{:keys [db]}]
   (let [current-timeout (get db :wallet/search-timeout)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db :wallet/local-suggestions [] :wallet/valid-ens-or-address? false)})))

(rf/reg-event-fx :wallet/select-send-address
 (fn [{:keys [db]} [address]]
   (println address "dsadass")
   {:db (assoc-in db [:wallet :ui :send :to-address] address)}))

(rf/reg-event-fx :wallet/get-address-details-success
 (fn [{:keys [db]} [{:keys [hasActivity]}]]
   {:db (assoc-in db
         [:wallet :ui :watch-address-activity-state]
         (if hasActivity :has-activity :no-activity))}))

(rf/reg-event-fx :wallet/clear-address-activity-check
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :watch-address-activity-state)}))

(rf/reg-event-fx :wallet/get-address-details
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet :ui :watch-address-activity-state] :scanning)
    :fx [[:json-rpc/call
          [{:method     "wallet_getAddressDetails"
            :params     [(chain/chain-id db) address]
            :on-success [:wallet/get-address-details-success]
            :on-error   #(log/info "failed to get address details"
                                   {:error %
                                    :event :wallet/get-address-details})}]]]}))
(rf/reg-event-fx :wallet/send-select-token
 (fn [{:keys [db]} [token stack-id]]
   {:db (assoc-in db [:wallet :ui :send :token] token)
    :fx [[:navigate-to-within-stack [:wallet-transaction-confirmation stack-id]]]}))

(rf/reg-event-fx :wallet/suggested-routes-success
 (fn [{:keys [db]} [transaction]]
   {:db (assoc-in db [:wallet :ui :send :suggested-routes] transaction)}))

(rf/reg-event-fx :wallet/get-suggested-routes
 (fn [{:keys [db]}]
   (let [wallet-address      (get-in db [:wallet :current-viewing-account-address])
         ;tokens              (get-in db [:wallet :accounts])
         ;account             (get-in db [:wallet :accounts wallet-address])
         token               (get-in db [:wallet :ui :send :token])
         to-address          (get-in db [:wallet :ui :send :to-address])
         token-decimal       18
         token-id            (:symbol token)
         value_              0.0005
         network-preferences [1]
         gas-rates           0 ;low
         amount-in           (money/mul (money/bignumber value_)
                                        (money/from-decimal token-decimal))
         from-address        wallet-address
         request-params      [0
                              from-address
                              to-address
                              (money/to-hex amount-in)
                              token-id
                              []
                              []
                              network-preferences
                              gas-rates
                              {}]]
     (prn "=====" request-params)
     {:json-rpc/call [{:method     "wallet_getSuggestedRoutes"
                       :params     request-params
                       :on-success (fn [suggested-routes]
                                     (println suggested-routes "suggested routes success")
                                     (rf/dispatch [:wallet/suggested-routes-success suggested-routes]))
                       :on-error   (fn [error]
                                     (log/error "failed to get suggested routes"
                                                {:event  :wallet/get-suggested-routes
                                                 :error  error
                                                 :params request-params}))}]})))

(rf/reg-event-fx :wallet/send-transaction
 (fn [{:keys [db]} [password]]
   (let [suggested-routes (get-in db [:wallet :ui :send :suggested-routes])
         from-address (get-in db [:wallet :current-viewing-account-address])
         to-address (get-in db [:wallet :ui :send :to-address])
         best (first (:Best suggested-routes))
         from (:From best)
         ;to (:To best)
         token (get-in db [:wallet :ui :send :token])
         token-id (:symbol token)
         from-asset token-id
         to-asset token-id
         bridge-name (:BridgeName best)
         chain-id (:chainId from)
         multi-transaction-command {:fromAddress from-address
                                    :toAddress   to-address
                                    :fromAsset   from-asset
                                    :toAsset     to-asset
                                    :fromAmount  (:AmountOut best)
                                    :type        0}

         transaction-bridge
         [{:BridgeName bridge-name
           :ChainID    chain-id
           :TransferTx {:From                 from-address
                        :To                   to-address
                        :Gas                  (money/to-hex (:GasAmount best))
                        :GasPrice             (money/to-hex (money/->wei :gwei
                                                                         (:gasPrice (:GasFees best))))
                        :Value                (:AmountOut best)
                        :Nonce                nil
                        :MaxFeePerGas         (money/to-hex
                                               (money/->wei :gwei (:maxFeePerGasMedium (:GasFees best))))
                        :MaxPriorityFeePerGas (money/to-hex (money/->wei :gwei
                                                                         (:maxPriorityFeePerGas (:GasFees
                                                                                                 best))))
                        :Input                ""
                        :Data                 "0x"}}]
         sha3-pwd (native-module/sha3 (str (security/safe-unmask-data password)))
         request-params [multi-transaction-command transaction-bridge sha3-pwd]]
     (prn "=====" request-params)
     {:json-rpc/call [{:method     "wallet_createMultiTransaction"
                       :params     request-params
                       :on-success #(prn % "======+++++++++========++++++++")
                       :on-error   (fn [error]
                                     (log/error "failed to send transaction"
                                                {:event  :wallet/send-transaction
                                                 :error  error
                                                 :params request-params}))}]})))
