(ns status-im.ethereum.tokens
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum])
  (:require-macros [status-im.ethereum.macros :as ethereum.macros :refer [resolve-icons]]))

(def default-native-currency
  (memoize
   (fn [symbol]
     {:name           "Native"
      :symbol         :ETH
      :symbol-display symbol
      :decimals       18
      :icon           {:source (js/require "../resources/images/tokens/default-token.png")}})))

(def snt-icon-source (js/require "../resources/images/tokens/mainnet/SNT.png"))

(def all-native-currencies
  (ethereum.macros/resolve-native-currency-icons
   {:mainnet     {:name     "Ether"
                  :symbol   :ETH
                  :decimals 18}
    :goerli      {:name           "Goerli Ether"
                  :symbol         :ETH
                  :symbol-display :ETHgo
                  :decimals       18}
    :xdai        {:name            "xDAI"
                  :symbol          :ETH
                  :symbol-display  :xDAI
                  :symbol-exchange :DAI
                  :decimals        18}
    :bsc         {:name           "BSC"
                  :symbol         :ETH
                  :symbol-display :BNB
                  :decimals       18}
    :bsc-testnet {:name           "BSC test"
                  :symbol         :ETH
                  :symbol-display :BNBtest
                  :decimals       18}}))

(def native-currency-symbols
  (set (map #(-> % val :symbol) all-native-currencies)))

(defn native-currency
  [{:keys [symbol] :as current-network}]
  (let [chain (ethereum/network->chain-keyword current-network)]
    (get all-native-currencies chain (default-native-currency symbol))))

(defn ethereum?
  [symbol]
  (native-currency-symbols symbol))

(def token-icons
  {:mainnet (resolve-icons :mainnet)
   :xdai    (resolve-icons :xdai)
   :custom  []})

(def default-token (js/require "../resources/images/tokens/default-token.png"))

(defn update-icon
  [network token]
  (-> token
      (assoc-in [:icon :source] (get-in token-icons [network (name (:symbol token))] default-token))
      (update :address string/lower-case)))

(defn nfts-for
  [all-tokens]
  (filter :nft? (vals all-tokens)))

(defn sorted-tokens-for
  [all-tokens]
  (->> (vals all-tokens)
       (filter #(not (:hidden? %)))
       (sort #(compare (string/lower-case (:name %1))
                       (string/lower-case (:name %2))))))

(defn symbol->token
  [all-tokens symbol]
  (some #(when (= symbol (:symbol %)) %) (vals all-tokens)))

(defn address->token
  [all-tokens address]
  (get all-tokens (string/lower-case address)))

(defn asset-for
  [all-tokens current-network symbol]
  (let [native-coin (native-currency current-network)]
    (if (or (= (:symbol-display native-coin) symbol)
            (= (:symbol native-coin) symbol))
      native-coin
      (symbol->token all-tokens symbol))))
