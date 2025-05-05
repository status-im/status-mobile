(ns status-im.contexts.wallet.swap.utils
  (:require [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [status-im.contexts.wallet.networks.config :as networks.config]
            [utils.i18n :as i18n]))

(defn error-message-from-code
  [error-code error-details]
  (cond
    (= error-code
       constants/router-error-code-not-enough-liquidity)
    (i18n/label :t/not-enough-liquidity)
    (= error-code
       constants/router-error-code-price-timeout)
    (i18n/label :t/fetching-the-price-took-longer-than-expected)
    (= error-code
       constants/router-error-code-price-impact-too-high)
    (i18n/label :t/price-impact-too-high)
    (= error-code
       constants/router-error-code-paraswap-custom-error)
    (i18n/label :t/paraswap-error
                {:paraswap-error error-details})
    (= error-code
       constants/router-error-code-generic)
    (i18n/label :t/generic-error
                {:generic-error error-details})
    (= error-code
       constants/router-error-code-not-enough-native-balance)
    (i18n/label :t/not-enough-assets-to-pay-gas-fees)
    :else
    (i18n/label :t/something-went-wrong-please-try-again-later)))

(defn current-viewing-account
  [wallet]
  (when-let [wallet-address (get wallet :current-viewing-account-address)]
    (get-in wallet [:accounts wallet-address])))

(defn select-asset-to-pay-by-symbol
  "Selects an asset to pay by token symbol.
   It's used for cases when only token symbol is available and the information
   about token needs to be extracted from the database.
   That happens when token is being selected on the home screen and
   it basically indicates that no account pre-selection was made."
  [{:keys [networks account token-symbol]}]
  (let [token (->> account
                   :tokens
                   (filter #(= token-symbol (:symbol %)))
                   first)]
    (assoc token
           :networks
           (network-utils/network-list-with-positive-balance
            token
            networks))))

(defn select-network
  "Chooses the network.
   Usually user needs to do the selection first and if the selection was done
   then the list of networks for the defined token will always contain
   one entry. Otherwise `nil` will be returned from here which will serve
   as an indicator that the network selector needs to be displayed."
  [{:keys [networks]}]
  (when (= (count networks) 1)
    (first networks)))

(defn updated-token-price
  "Returns the updated token price (number) from the routes."
  [token updated-prices]
  (let [token-symbol (some-> token
                             :symbol
                             keyword)]
    (get updated-prices token-symbol 0)))

(defn get-default-asset-to-receive
  [tokens pay-token-symbol chain-id]
  (let [asset-to-receive
        (cond
          ;; Temporary workaround as USDC has 6 decimals on ETH/OPT/ARB/BASE and 18 decimals on BSC
          (and (= pay-token-symbol "ETH") (not= chain-id networks.config/bsc-chain-id)) "USDC (EVM)"
          (and (= pay-token-symbol "BNB") (= chain-id networks.config/bsc-chain-id))    "USDC (BSC)"
          (and (not= pay-token-symbol "BNB") (= chain-id networks.config/bsc-chain-id)) "BNB"
          (= pay-token-symbol "SNT")                                                    "ETH"
          :else                                                                         "SNT")]
    (some #(when (and (= (:symbol %) asset-to-receive) (= (:chain-id %) chain-id)) %) tokens)))
