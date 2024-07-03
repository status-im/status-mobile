(ns status-im.subs.wallet.wallet-connect
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            [status-im.contexts.wallet.common.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [utils.money :as money]))

(rf/reg-sub
 :wallet-connect/current-request-address
 :<- [:wallet-connect/current-request]
 :-> :address)

(rf/reg-sub
 :wallet-connect/current-request-display-data
 :<- [:wallet-connect/current-request]
 :-> :display-data)

(rf/reg-sub
 :wallet-connect/account-details-by-address
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts [_ address]]
   (let [{:keys [customization-color name emoji]} (wallet-utils/get-account-by-address accounts address)]
     {:customization-color customization-color
      :name                name
      :emoji               emoji})))

(rf/reg-sub
 :wallet-connect/current-request-account-details
 :<- [:wallet-connect/current-request-address]
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [[address accounts]]
   (let [{:keys [customization-color name emoji]} (wallet-utils/get-account-by-address accounts address)]
     {:customization-color customization-color
      :name                name
      :emoji               emoji})))

(rf/reg-sub
 :wallet-connect/current-request-dapp
 :<- [:wallet-connect/current-request]
 :<- [:wallet-connect/pairings]
 (fn [[request pairings]]
   (let [dapp-url (get-in request [:event :verifyContext :verified :origin])]
     (->> pairings
          (filter (fn [pairing]
                    (= dapp-url (get-in pairing [:peerMetadata :url]))))
          (first)))))

(rf/reg-sub
 :wallet-connect/current-request-network
 :<- [:wallet-connect/current-request]
 (fn [request]
   (-> request
       (get-in [:raw-data :params :chainId])
       (wallet-connect-core/eip155->chain-id)
       (networks/get-network-details))))

(rf/reg-sub
 :wallet-connect/current-request-transaction-information
 :<- [:wallet-connect/current-request]
 :<- [:wallet/accounts]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[request accounts currency currency-symbol]]
   (let [chain-id                          (-> request
                                               (get-in [:raw-data :params :chainId])
                                               (wallet-connect-core/eip155->chain-id))
         all-tokens                        (->> accounts
                                                (filter #(= (:address %)
                                                            (:address request)))
                                                (first)
                                                :tokens)
         eth-token                         (->> all-tokens
                                                (filter #(= (:symbol %) "ETH"))
                                                (first))
         {:keys [gasPrice gasLimit value]} (-> request
                                               :raw-data
                                               wallet-connect-core/get-request-params
                                               first)
         max-fees-wei                      (money/bignumber (* gasPrice gasLimit))]
     (when (and gasPrice gasLimit)
       (let [max-fees-ether          (money/wei->ether max-fees-wei)
             token-fiat-value        (wallet-utils/calculate-token-fiat-value {:currency currency
                                                                               :balance  max-fees-ether
                                                                               :token    eth-token})
             crypto-formatted        (wallet-utils/get-standard-crypto-format eth-token max-fees-ether)
             fiat-formatted          (wallet-utils/get-standard-fiat-format crypto-formatted
                                                                            currency-symbol
                                                                            token-fiat-value)
             balance                 (-> eth-token
                                         (get-in [:balances-per-chain chain-id :raw-balance])
                                         (money/bignumber))
             value                   (money/bignumber value)
             total-transaction-value (money/add max-fees-wei value)]
         {:total-transaction-value total-transaction-value
          :balance                 balance
          :max-fees                max-fees-wei
          :max-fees-fiat-value     token-fiat-value
          :max-fees-fiat-formatted fiat-formatted
          :error-state             (cond
                                     (and
                                      (money/sufficient-funds? value balance)
                                      (not (money/sufficient-funds? total-transaction-value balance)))
                                     :not-enough-assets-to-pay-gas-fees

                                     (not (money/sufficient-funds? value balance))
                                     :not-enough-assets

                                     :else nil)})))))

(rf/reg-sub
 :wallet-connect/current-proposal-request
 :<- [:wallet-connect/current-proposal]
 :-> :request)

(rf/reg-sub
 :wallet-connect/session-proposal-networks
 :<- [:wallet-connect/current-proposal]
 :-> :session-networks)

(rf/reg-sub
 :wallet-connect/session-proposer
 :<- [:wallet-connect/current-proposal-request]
 (fn [proposal]
   (-> proposal :params :proposer)))

(rf/reg-sub
 :wallet-connect/session-proposer-name
 :<- [:wallet-connect/session-proposer]
 (fn [proposer]
   (-> proposer :metadata :name)))

(rf/reg-sub
 :wallet-connect/session-proposal-network-details
 :<- [:wallet-connect/session-proposal-networks]
 :<- [:wallet/network-details]
 (fn [[session-networks network-details]]
   (let [supported-networks       (map :chain-id network-details)
         session-networks         (filterv #(contains? (set session-networks) (:chain-id %))
                                           network-details)
         all-networks-in-session? (= (count supported-networks) (count session-networks))]
     {:session-networks         session-networks
      :all-networks-in-session? all-networks-in-session?})))

(rf/reg-sub
 :wallet-connect/current-proposal-address
 (fn [db]
   (get-in db [:wallet-connect/current-proposal :address])))
