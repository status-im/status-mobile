(ns legacy.status-im.ethereum.tokens
  (:require
    [utils.ethereum.chain :as chain])
  (:require-macros [legacy.status-im.ethereum.macros :as ethereum.macros]))

(def default-native-currency
  (memoize
   (fn [sym]
     {:name           "Native"
      :symbol         :ETH
      :symbol-display sym
      :decimals       18
      :icon           {:source (js/require "../resources/images/tokens/default-token.png")}})))

(def all-native-currencies
  (ethereum.macros/resolve-native-currency-icons
   {:mainnet     {:name     "Ether"
                  :symbol   :ETH
                  :decimals 18}
    :sepolia     {:name           "Sepolia Ether"
                  :symbol         :ETH
                  :symbol-display :ETH
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

(defn native-currency
  [{sym :symbol :as current-network}]
  (let [chain (chain/network->chain-keyword current-network)]
    (get all-native-currencies chain (default-native-currency sym))))

