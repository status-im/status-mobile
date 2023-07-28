(ns status-im2.subs.wallet.wallet
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.currency :as currency]
            [utils.money :as money]
            [status-im2.config :as config]
            [utils.i18n :as i18n]))

(re-frame/reg-sub
 :balance
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:accounts address :balance])))

(re-frame/reg-sub
 :balance-default
 :<- [:wallet]
 :<- [:profile/wallet-accounts]
 (fn [[wallet accounts]]
   (get-in wallet [:accounts (:address (ethereum/get-default-account accounts)) :balance])))

(re-frame/reg-sub
 :balances
 :<- [:wallet]
 :<- [:multiaccount/visible-accounts]
 (fn [[wallet accounts]]
   (let [accounts (map :address accounts)]
     (map :balance (vals (select-keys (:accounts wallet) accounts))))))

(re-frame/reg-sub
 :empty-balances?
 :<- [:balances]
 (fn [balances]
   (every?
    (fn [balance]
      (every?
       (fn [^js asset]
         (or (nil? asset) (.isZero asset)))
       (vals balance)))
    balances)))

(re-frame/reg-sub
 :price
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym])))

(re-frame/reg-sub
 :last-day
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym :last-day])))

(re-frame/reg-sub
 :wallet.settings/currency
 :<- [:profile/profile]
 (fn [settings]
   (or (get settings :currency) :usd)))

(defn get-balance-total-value
  [balance prices currency token->decimals]
  (reduce-kv (fn [acc symbol value]
               (if-let [price (get-in prices [symbol currency])]
                 (+ acc
                    (or (some-> (money/internal->formatted value symbol (token->decimals symbol))
                                ^js (money/crypto->fiat price)
                                .toNumber)
                        0))
                 acc))
             0
             balance))

(re-frame/reg-sub
 :wallet/token->decimals
 :<- [:wallet/all-tokens]
 (fn [all-tokens]
   (into {} (map #(vector (:symbol %) (:decimals %)) (vals all-tokens)))))

(re-frame/reg-sub
 :portfolio-value
 :<- [:balances]
 :<- [:prices]
 :<- [:wallet/currency]
 :<- [:wallet/token->decimals]
 (fn [[balances prices currency token->decimals]]
   (if (and balances prices)
     (let [currency-key        (-> currency :code keyword)
           balance-total-value (apply
                                +
                                (map #(get-balance-total-value % prices currency-key token->decimals)
                                     balances))]
       (if (pos? balance-total-value)
         (-> balance-total-value
             (money/with-precision 2)
             str
             (i18n/format-currency (:code currency)))
         "0"))
     "...")))

(re-frame/reg-sub
 :account-portfolio-value
 (fn [[_ address] _]
   [(re-frame/subscribe [:balance address])
    (re-frame/subscribe [:prices])
    (re-frame/subscribe [:wallet/currency])
    (re-frame/subscribe [:wallet/token->decimals])])
 (fn [[balance prices currency token->decimals]]
   (if (and balance prices)
     (let [currency-key        (-> currency :code keyword)
           balance-total-value (get-balance-total-value balance prices currency-key token->decimals)]
       (if (pos? balance-total-value)
         (-> balance-total-value
             (money/with-precision 2)
             str
             (i18n/format-currency (:code currency)))
         "0"))
     "...")))

(re-frame/reg-sub
 :wallet/sorted-tokens
 :<- [:wallet/all-tokens]
 (fn [all-tokens]
   (tokens/sorted-tokens-for all-tokens)))

(re-frame/reg-sub
 :wallet/grouped-chain-tokens
 :<- [:wallet/sorted-tokens]
 :<- [:wallet/visible-tokens-symbols]
 (fn [[all-tokens visible-tokens]]
   (let [vt-set (set visible-tokens)]
     (group-by :custom?
               (map #(assoc % :checked? (boolean (get vt-set (keyword (:symbol %))))) all-tokens)))))

(re-frame/reg-sub
 :wallet/fetching-tx-history?
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:fetching address :history?])))

(re-frame/reg-sub
 :wallet/fetching-recent-tx-history?
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:fetching address :recent?])))

(re-frame/reg-sub
 :wallet/tx-history-fetched?
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:fetching address :all-fetched?])))

(re-frame/reg-sub
 :wallet/chain-explorer-link
 (fn [db [_ address]]
   (let [network (:networks/current-network db)
         link    (get-in config/default-networks-by-id
                         [network :chain-explorer-link])]
     (when link
       (str link address)))))

(re-frame/reg-sub
 :wallet/error-message
 :<- [:wallet]
 (fn [wallet]
   (or (get-in wallet [:errors :balance-update])
       (get-in wallet [:errors :prices-update]))))

(re-frame/reg-sub
 :wallet/visible-tokens-symbols
 :<- [:ethereum/chain-keyword]
 :<- [:profile/profile]
 (fn [[chain current-multiaccount]]
   (get-in current-multiaccount [:wallet/visible-tokens chain])))

(re-frame/reg-sub
 :wallet/visible-assets
 :<- [:current-network]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:wallet/sorted-tokens]
 (fn [[network visible-tokens-symbols all-tokens-sorted]]
   (conj (filter #(contains? visible-tokens-symbols (:symbol %)) all-tokens-sorted)
         (tokens/native-currency network))))

(re-frame/reg-sub
 :wallet/visible-assets-with-amount
 (fn [[_ address] _]
   [(re-frame/subscribe [:balance address])
    (re-frame/subscribe [:wallet/visible-assets])])
 (fn [[balance visible-assets]]
   (map #(assoc % :amount (get balance (:symbol %))) visible-assets)))

(defn update-value
  [prices currency]
  (fn [{:keys [symbol decimals amount] :as token}]
    (let [currency-kw (-> currency :code keyword)
          price       (get-in prices [symbol currency-kw])]
      (assoc token
             :price price
             :value (when (and amount price)
                      (-> (money/internal->formatted amount symbol decimals)
                          (money/crypto->fiat price)
                          (money/with-precision 2)
                          str
                          (i18n/format-currency (:code currency))))))))

(re-frame/reg-sub
 :wallet/visible-assets-with-values
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet/visible-assets-with-amount address])
    (re-frame/subscribe [:prices])
    (re-frame/subscribe [:wallet/currency])])
 (fn [[assets prices currency]]
   (let [{:keys [tokens nfts]} (group-by #(if (:nft? %) :nfts :tokens) assets)
         tokens-with-values    (map (update-value prices currency) tokens)]
     {:tokens tokens-with-values
      :nfts   nfts})))

(defn get-asset-amount
  [balances sym]
  (reduce #(if-let [^js bl (get %2 sym)]
             (.plus ^js (or ^js %1 ^js (money/bignumber 0)) bl)
             %1)
          nil
          balances))

(re-frame/reg-sub
 :wallet/all-visible-assets-with-amount
 :<- [:balances]
 :<- [:wallet/visible-assets]
 (fn [[balances visible-assets]]
   (map #(assoc % :amount (get-asset-amount balances (:symbol %))) visible-assets)))

(re-frame/reg-sub
 :wallet/all-visible-assets-with-values
 :<- [:wallet/all-visible-assets-with-amount]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[assets prices currency]]
   (let [{:keys [tokens nfts]} (group-by #(if (:nft? %) :nfts :tokens) assets)
         tokens-with-values    (map (update-value prices currency) tokens)]
     {:tokens tokens-with-values
      :nfts   nfts})))

(re-frame/reg-sub
 :wallet/transferrable-assets-with-amount
 (fn [[_ address]]
   (re-frame/subscribe [:wallet/visible-assets-with-amount address]))
 (fn [all-assets]
   (filter #(not (:nft? %)) all-assets)))

(re-frame/reg-sub
 :wallet/currency
 :<- [:wallet.settings/currency]
 (fn [currency-id]
   (get currency/currencies currency-id (get currency/currencies :usd))))

(defn filter-recipient-favs
  [search-filter {:keys [name]}]
  (string/includes? (string/lower-case (str name)) search-filter))

(re-frame/reg-sub
 :wallet/favourites-filtered
 :<- [:wallet/favourites]
 :<- [:search/recipient-filter]
 (fn [[favs search-filter]]
   (let [favs (vals favs)]
     (if (string/blank? search-filter)
       favs
       (filter (partial filter-recipient-favs
                        (string/lower-case search-filter))
               favs)))))

(re-frame/reg-sub
 :wallet/collectible-collection
 :<- [:wallet/collectible-collections]
 (fn [all-collections [_ address]]
   (when address
     (let [all-collections (get all-collections (string/lower-case address) [])]
       (sort-by :name all-collections)))))

(re-frame/reg-sub
 :wallet/collectible-assets-by-collection-and-address
 :<- [:wallet/collectible-assets]
 (fn [all-assets [_ address collectible-slug]]
   (get-in all-assets [address collectible-slug] [])))

(re-frame/reg-sub
 :wallet/fetching-assets-by-collectible-slug
 :<- [:wallet/fetching-collection-assets]
 (fn [fetching-collection-assets [_ collectible-slug]]
   (get fetching-collection-assets collectible-slug false)))
