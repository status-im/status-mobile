(ns status-im.contexts.wallet.data-store
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as collectible-utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
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

(defn- rpc->balances-per-chain
  [token]
  (-> token
      (update :balances-per-chain update-vals #(update % :raw-balance money/bignumber))
      (update :balances-per-chain update-keys (comp utils.number/parse-int name))))

(defn- remove-tokens-with-empty-values
  [tokens]
  (remove
   #(or (string/blank? (:symbol %)) (string/blank? (:name %)))
   tokens))

(defn rpc->tokens
  [tokens]
  (-> tokens
      (update-keys name)
      (update-vals #(cske/transform-keys transforms/->kebab-case-keyword %))
      (update-vals remove-tokens-with-empty-values)
      (update-vals #(mapv rpc->balances-per-chain %))))

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
  (-> saved-address
      (assoc :network-preferences-names
             (network-utils/network-preference-prefix->network-names (:chain-short-names saved-address)))
      (assoc :ens? (not (string/blank? (:ens saved-address))))))

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

(defn new->old-route-path
  [new-path]
  (let [bonder-fees        (:tx-bonder-fees new-path)
        estimated-received (- (:amount-in new-path) (:tx-token-fees new-path))
        token-fees         (+ (money/wei->ether bonder-fees)
                              (money/wei->ether (:tx-token-fees new-path)))]
    {:from                      (:from-chain new-path)
     :amount-in-locked          (:amount-in-locked new-path)
     :amount-in                 (:amount-in new-path)
     :estimated-received        estimated-received
     :max-amount-in             (:max-amount-in new-path)
     :gas-fees                  {:gas-price                "0"
                                 :base-fee                 (send-utils/convert-to-gwei (:tx-base-fee
                                                                                        new-path)
                                                                                       precision)
                                 :max-priority-fee-per-gas (send-utils/convert-to-gwei (:tx-priority-fee
                                                                                        new-path)
                                                                                       precision)
                                 :max-fee-per-gas-low      (send-utils/convert-to-gwei
                                                            (get-in
                                                             new-path
                                                             [:suggested-levels-for-max-fees-per-gas
                                                              :low])
                                                            precision)
                                 :max-fee-per-gas-medium   (send-utils/convert-to-gwei
                                                            (get-in
                                                             new-path
                                                             [:suggested-levels-for-max-fees-per-gas
                                                              :medium])
                                                            precision)
                                 :max-fee-per-gas-high     (send-utils/convert-to-gwei
                                                            (get-in
                                                             new-path
                                                             [:suggested-levels-for-max-fees-per-gas
                                                              :high])
                                                            precision)
                                 :l-1-gas-fee              (send-utils/convert-to-gwei (:tx-l-1-fee
                                                                                        new-path)
                                                                                       precision)
                                 :eip-1559-enabled         true}
     :bridge-name               (:processor-name new-path)
     :amount-out                (:amount-out new-path)
     :approval-contract-address (:approval-contract-address new-path)
     :approval-required         (:approval-required new-path)
     :estimated-time            (:estimated-time new-path)
     :approval-gas-fees         (* (money/wei->ether (get-in new-path
                                                             [:suggested-levels-for-max-fees-per-gas
                                                              :medium]))
                                   (:approval-gas-amount new-path))
     :to                        (:to-chain new-path)
     :bonder-fees               bonder-fees
     :approval-amount-required  (:approval-amount-required new-path)
     ;;  :cost () ;; tbd not used on desktop
     :token-fees                token-fees
     :gas-amount                (:tx-gas-amount new-path)}))

(defn tokens-never-loaded?
  [db]
  (nil? (get-in db [:wallet :ui :tokens-loading])))

(defn rpc->collectibles
  [collectibles]
  (->> collectibles
       (cske/transform-keys transforms/->kebab-case-keyword)
       (map #(assoc % :unique-id (collectible-utils/get-collectible-unique-id %)))
       vec))
