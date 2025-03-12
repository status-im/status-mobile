(ns status-im.contexts.wallet.data-store
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as collectible-utils]
    [status-im.contexts.wallet.send.transaction-settings.core :as transaction-settings]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [utils.collection :as utils.collection]
    [utils.money :as money]
    [utils.number :as utils.number]
    [utils.transforms :as transforms]))

(defn chain-ids-string->set
  [ids-string]
  (into #{}
        (map utils.number/parse-int)
        (string/split ids-string constants/chain-id-separator)))

(defn chain-ids-set->string
  [ids]
  (string/join constants/chain-id-separator ids))

(defn add-keys-to-account
  [account]
  (-> account
      (assoc :operable? (not= (:operable account) :no))
      (assoc :watch-only? (= (:type account) :watch))
      (assoc :default-account? (:wallet account))))

(defn- sanitize-emoji
  "As Desktop uses Twemoji, the emoji received can be an img tag
   with raw emoji in alt attribute. This function help us to extract
   the emoji from it as mobile doesn't support HTML rendering and Twemoji"
  [emoji]
  (if (string/starts-with? emoji "<img")
    (-> (re-find #"alt=\"(.*?)\"" emoji) last)
    emoji))

(defn rpc->account
  [account]
  (-> account
      (set/rename-keys {:prodPreferredChainIds :prod-preferred-chain-ids
                        :testPreferredChainIds :test-preferred-chain-ids
                        :createdAt             :created-at
                        :colorId               :color})
      (update :prod-preferred-chain-ids chain-ids-string->set)
      (update :test-preferred-chain-ids chain-ids-string->set)
      (update :type keyword)
      (update :operable keyword)
      (update :color
              #(if (and (not (keyword? %)) (string/blank? %))
                 constants/account-default-customization-color
                 (keyword %)))
      (update :emoji sanitize-emoji)
      add-keys-to-account))

(defn rpc->accounts
  [accounts]
  (->> (filter #(not (:chat %)) accounts)
       (sort-by :position)
       (map rpc->account)))

(defn <-account
  [account]
  (-> account
      (set/rename-keys {:prod-preferred-chain-ids :prodPreferredChainIds
                        :test-preferred-chain-ids :testPreferredChainIds
                        :color                    :colorId})
      (update :prodPreferredChainIds chain-ids-set->string)
      (update :testPreferredChainIds chain-ids-set->string)
      (dissoc :watch-only? :default-account? :operable? :tokens :collectibles)))

(defn add-missing-balance-per-chain-values
  "Adds any missing chain balance (0) to the balance-per-chain map based on token supported chains as
   status-go returns balance for that chain only if the balance is positive."
  [balances-per-chain supported-chains]
  (let [zero-balance-per-chain (fn [chain-id]
                                 {:chain-id    chain-id
                                  :raw-balance (money/->bignumber 0)
                                  :balance     "0"})]
    (reduce
     (fn [result chain-id]
       (if (contains? result chain-id)
         result
         (assoc result chain-id (zero-balance-per-chain chain-id))))
     balances-per-chain
     supported-chains)))

(defn- rpc->balances-per-chain
  [token supported-chains]
  (-> token
      (update :balances-per-chain update-vals #(update % :raw-balance money/bignumber))
      (update :balances-per-chain update-keys (comp utils.number/parse-int name))
      (update :balances-per-chain #(add-missing-balance-per-chain-values % supported-chains))))

(defn- update-balances-per-chain
  [tokens supported-chains-by-token-symbol]
  (mapv #(rpc->balances-per-chain % (get supported-chains-by-token-symbol (:symbol %))) tokens))

(defn- remove-tokens-with-empty-values
  [tokens]
  (remove
   #(or (string/blank? (:symbol %)) (string/blank? (:name %)))
   tokens))

(defn rpc->tokens
  [tokens supported-chains-by-token-symbol]
  (-> tokens
      (update-keys name)
      (update-vals #(cske/transform-keys transforms/->kebab-case-keyword %))
      (update-vals remove-tokens-with-empty-values)
      (update-vals #(update-balances-per-chain % supported-chains-by-token-symbol))))

(defn rpc->network
  [network]
  (-> network
      (set/rename-keys
       {:Prod                   :prod
        :Test                   :test
        :isTest                 :test?
        :tokenOverrides         :token-overrides
        :rpcUrl                 :rpc-url
        :chainColor             :chain-color
        :chainName              :chain-name
        :nativeCurrencyDecimals :native-currency-decimals
        :relatedChainId         :related-chain-id
        :shortName              :short-name
        :chainId                :chain-id
        :originalFallbackURL    :original-fallback-url
        :originalRpcUrl         :original-rpc-url
        :fallbackURL            :fallback-url
        :blockExplorerUrl       :block-explorer-url
        :nativeCurrencySymbol   :native-currency-symbol
        :nativeCurrencyName     :native-currency-symbol})))

(defn partially-operable-accounts?
  [accounts]
  (->> accounts
       (some #(= :partially (:operable %)))
       boolean))

(defn get-keypair-lowest-operability
  [{:keys [accounts]}]
  (cond
    (some #(= (:operable %) :no) accounts)
    :no

    (some #(= (:operable %) :partially) accounts)
    :partially

    :else
    :fully))

(defn- add-keys-to-keypair
  [keypair]
  (assoc keypair :lowest-operability (get-keypair-lowest-operability keypair)))

(defn rpc->keypair
  [keypair]
  (-> keypair
      (update :type keyword)
      (update :accounts #(map rpc->account %))
      add-keys-to-keypair))

(defn rpc->keypairs
  [keypairs]
  (->> (map rpc->keypair keypairs)
       (sort-by #(if (= (:type %) :profile) 0 1))))

(defn- add-keys-to-saved-address
  [saved-address]
  (assoc saved-address :ens? (not (string/blank? (:ens saved-address)))))

(defn rpc->saved-address
  [saved-address]
  (-> saved-address
      (set/rename-keys {:chainShortNames  :chain-short-names
                        :isTest           :test?
                        :createdAt        :created-at
                        :colorId          :customization-color
                        :mixedcaseAddress :mixedcase-address
                        :removed          :removed?})
      (update :customization-color (comp keyword string/lower-case))
      add-keys-to-saved-address))

(defn rpc->saved-addresses
  [saved-addresses]
  (map rpc->saved-address saved-addresses))

(defn reconcile-keypairs
  [keypairs]
  (let [received-keypairs             (rpc->keypairs keypairs)
        keypair-label                 #(if % :removed-keypairs :updated-keypairs)
        {:keys [removed-keypairs
                updated-keypairs]
         :or   {updated-keypairs []
                removed-keypairs []}} (group-by (comp keypair-label :removed) received-keypairs)
        updated-keypairs-by-id        (utils.collection/index-by :key-uid updated-keypairs)
        updated-accounts-by-address   (transduce (comp (mapcat :accounts)
                                                       (filter (comp not :chat))
                                                       (map #(vector (:address %) %)))
                                                 conj
                                                 {}
                                                 updated-keypairs)
        removed-keypairs-ids          (set (map :key-uid removed-keypairs))
        removed-account-addresses     (transduce (comp (mapcat :accounts)
                                                       (map :address))
                                                 conj
                                                 #{}
                                                 removed-keypairs)]
    {:removed-keypair-ids         removed-keypairs-ids
     :removed-account-addresses   removed-account-addresses
     :updated-keypairs-by-id      updated-keypairs-by-id
     :updated-accounts-by-address updated-accounts-by-address}))

(defn- rename-keys-to-kebab-case
  [m]
  (set/rename-keys m (zipmap (keys m) (map transforms/->kebab-case-keyword (keys m)))))

(defn rpc->suggested-routes
  [suggested-routes]
  (cond
    (map? suggested-routes)
    (into {}
          (for [[k v] (rename-keys-to-kebab-case suggested-routes)]
            [k (rpc->suggested-routes v)]))

    (vector? suggested-routes)
    (map rpc->suggested-routes suggested-routes)

    :else suggested-routes))

(def ^:private precision 6)

(defn from-wei
  [wei-amount]
  (send-utils/convert-to-gwei wei-amount precision))

(defn new->old-route-path
  [new-path]
  (let [to-bignumber                          (fn [k] (-> new-path k money/bignumber))
        suggested-levels-for-max-fees-per-gas (:suggested-levels-for-max-fees-per-gas new-path)]
    {:approval-fee              (to-bignumber :approval-fee)
     :approval-l-1-fee          (to-bignumber :approval-l-1-fee)
     :bonder-fees               (to-bignumber :tx-bonder-fees)
     :token-fees                (to-bignumber :tx-token-fees)
     :from                      (:from-chain new-path)
     :amount-in-locked          (:amount-in-locked new-path)
     :amount-in                 (:amount-in new-path)
     :max-amount-in             (:max-amount-in new-path)
     :gas-fees                  {:gas-price "0"
                                 :base-fee (from-wei (:tx-base-fee new-path))
                                 :tx-priority-fee (from-wei (:tx-priority-fee new-path))
                                 :l-1-gas-fee (from-wei (:tx-l-1-fee new-path))
                                 :eip-1559-enabled true
                                 :tx-max-fees-per-gas (from-wei (:tx-max-fees-per-gas new-path))
                                 :suggested-min-priority-fee (from-wei (:suggested-min-priority-fee
                                                                        new-path))
                                 :suggested-max-priority-fee (from-wei (:suggested-max-priority-fee
                                                                        new-path))
                                 :suggested-gas-fees-for-setting
                                 {:tx-fee-mode/normal (from-wei
                                                       (:low
                                                        suggested-levels-for-max-fees-per-gas))
                                  :tx-fee-mode/fast   (from-wei
                                                       (:medium
                                                        suggested-levels-for-max-fees-per-gas))
                                  :tx-fee-mode/urgent (from-wei
                                                       (:high
                                                        suggested-levels-for-max-fees-per-gas))}}
     :bridge-name               (:processor-name new-path)
     :amount-out                (:amount-out new-path)
     :approval-contract-address (:approval-contract-address new-path)
     :approval-required         (:approval-required new-path)
     :estimated-time            (:estimated-time new-path)
     :to                        (:to-chain new-path)
     :approval-amount-required  (:approval-amount-required new-path)
     :gas-amount                (:tx-gas-amount new-path)
     :router-input-params-uuid  (:router-input-params-uuid new-path)
     :tx-fee-mode               (transaction-settings/gas-rate->tx-fee-mode (:tx-gas-fee-mode
                                                                             new-path))
     :nonce                     (-> new-path
                                    :tx-nonce
                                    money/from-hex
                                    money/to-string
                                    js/parseInt)
     :suggested-tx-nonce        (-> new-path
                                    :suggested-tx-nonce
                                    money/from-hex
                                    money/to-string
                                    js/parseInt)
     :suggested-tx-gas-amount   (:suggested-tx-gas-amount new-path)}))

(defn tokens-never-loaded?
  [db]
  (nil? (get-in db [:wallet :ui :tokens-loading])))

(defn rpc->collectibles
  [collectibles]
  (->> collectibles
       (cske/transform-keys transforms/->kebab-case-keyword)
       (map #(update % :ownership collectible-utils/remove-duplicates-in-ownership))
       (map #(assoc % :unique-id (collectible-utils/get-collectible-unique-id %)))
       vec))

(defn selected-keypair-keycard?
  [db]
  (let [keypairs             (get-in db [:wallet :keypairs])
        selected-keypair-uid (get-in db [:wallet :ui :create-account :selected-keypair-uid])
        keypair              (get keypairs selected-keypair-uid)]
    (boolean (seq (:keycards keypair)))))

(defn- transform-collectible
  [{:keys [added updated removed]}]
  (let [entry (first (remove nil? (concat added updated removed)))]
    (when entry
      {:contract-id {:chain-id (:chainID (:contractID entry))
                     :address  (:address (:contractID entry))}
       :token-id    (str (:tokenID entry))})))

(defn rpc->collectible-id
  [collectible]
  (-> collectible
      transforms/json->clj
      transform-collectible))
