(ns status-im.contexts.wallet.swap.utils
  (:require [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
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
  [{:keys [wallet account test-networks-enabled? token-symbol]}]
  (let [networks (-> (get-in wallet [:networks (if test-networks-enabled? :test :prod)])
                     (network-utils/sorted-networks-with-details))
        token    (->> account
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
