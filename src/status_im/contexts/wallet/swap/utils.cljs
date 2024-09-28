(ns status-im.contexts.wallet.swap.utils
  (:require [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
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

(defn select-asset-to-receive
  [wallet profile asset-to-pay]
  (let [wallet-address         (get-in wallet [:current-viewing-account-address])
        account                (-> wallet
                                   :accounts
                                   vals
                                   (utils/get-account-by-address wallet-address))
        test-networks-enabled? (get profile :test-networks-enabled?)
        networks               (-> (get-in wallet
                                           [:wallet :networks (if test-networks-enabled? :test :prod)])
                                   (network-utils/sorted-networks-with-details))]
    (->> (utils/tokens-with-balance (:tokens account) networks nil)
         (remove #(= (:symbol %) (:symbol asset-to-pay)))
         first)))

(defn select-network
  [{:keys [networks]}]
  (when (= (count networks) 1)
    (first networks)))
