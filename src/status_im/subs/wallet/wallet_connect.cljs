(ns status-im.subs.wallet.wallet-connect
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [status-im.contexts.wallet.wallet-connect.transactions :as transactions]
            [utils.money :as money]
            [utils.string]))

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
 :<- [:wallet-connect/sessions]
 (fn [[request sessions]]
   (let [dapp-url (get-in request [:event :verifyContext :verified :origin])]
     (->> sessions
          (filter (fn [session]
                    (= (utils.string/remove-trailing-slash dapp-url)
                       (utils.string/remove-trailing-slash (get session :url)))))
          (first)))))

(rf/reg-sub
 :wallet-connect/sessions-for-current-account
 :<- [:wallet-connect/sessions]
 :<- [:wallet/current-viewing-account-address]
 (fn [[sessions address]]
   (filter
    (fn [{:keys [accounts]}]
      (some #(string/includes? % address) accounts))
    sessions)))

(rf/reg-sub
 :wallet-connect/sessions-for-current-account-and-networks
 :<- [:wallet-connect/sessions-for-current-account]
 :<- [:profile/test-networks-enabled?]
 (fn [[sessions testnet-mode?]]
   (filter
    (partial wallet-connect-core/session-networks-allowed? testnet-mode?)
    sessions)))

(rf/reg-sub
 :wallet-connect/chain-id
 :<- [:wallet-connect/current-request]
 (fn [request]
   (-> request
       (get-in [:event :params :chainId])
       (wallet-connect-core/eip155->chain-id))))

(rf/reg-sub
 :wallet-connect/current-request-network
 :<- [:wallet-connect/chain-id]
 wallet-connect-core/chain-id->network-details)

(rf/reg-sub
 :wallet-connect/transaction-args
 :<- [:wallet-connect/current-request]
 (fn [{:keys [event transaction]}]
   (when (transactions/transaction-request? event)
     transaction)))

(rf/reg-sub
 :wallet-connect/transaction-suggested-fees
 :<- [:wallet-connect/current-request]
 (fn [{:keys [event raw-data]}]
   (when (transactions/transaction-request? event)
     (:suggested-fees raw-data))))

(rf/reg-sub
 :wallet-connect/transaction-max-fees-wei
 :<- [:wallet-connect/transaction-args]
 :<- [:wallet-connect/transaction-suggested-fees]
 (fn [[transaction suggested-fees]]
   (when transaction
     (let [{:keys [gasPrice gas gasLimit maxFeePerGas]} transaction
           eip-1559-chain?                              (:eip1559Enabled suggested-fees)
           gas-limit                                    (or gasLimit gas)
           max-gas-fee                                  (if eip-1559-chain? maxFeePerGas gasPrice)]
       (money/bignumber (* max-gas-fee gas-limit))))))

(rf/reg-sub
 :wallet-connect/account-eth-token
 :<- [:wallet-connect/current-request-address]
 :<- [:wallet/accounts]
 (fn [[address accounts]]
   (let [fee-token    "ETH"
         find-account #(when (= (:address %) address) %)
         find-token   #(when (= (:symbol %) fee-token) %)]
     (->> accounts
          (some find-account)
          :tokens
          (some find-token)))))

(rf/reg-sub
 :wallet-connect/current-request-transaction-information
 :<- [:wallet-connect/chain-id]
 :<- [:wallet-connect/transaction-max-fees-wei]
 :<- [:wallet-connect/transaction-args]
 :<- [:wallet-connect/account-eth-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[chain-id max-fees-wei transaction eth-token currency currency-symbol]]
   (when transaction
     (let [max-fees-ether          (money/wei->ether max-fees-wei)
           max-fees-fiat           (wallet-utils/calculate-token-fiat-value {:currency currency
                                                                             :balance  max-fees-ether
                                                                             :token    eth-token})
           max-fees-fiat-formatted (-> max-fees-ether
                                       (wallet-utils/get-standard-crypto-format eth-token)
                                       (wallet-utils/get-standard-fiat-format currency-symbol
                                                                              max-fees-fiat))
           balance                 (-> eth-token
                                       (get-in [:balances-per-chain chain-id :raw-balance])
                                       money/bignumber)
           tx-value                (money/bignumber (:value transaction))
           total-transaction-value (money/add max-fees-wei tx-value)]
       {:total-transaction-value total-transaction-value
        :balance                 balance
        :max-fees                max-fees-wei
        :max-fees-fiat-value     max-fees-fiat
        :max-fees-fiat-formatted max-fees-fiat-formatted
        :error-state             (cond
                                   (not (money/sufficient-funds? tx-value balance))
                                   :not-enough-assets

                                   (not (money/sufficient-funds? total-transaction-value
                                                                 balance))
                                   :not-enough-assets-to-pay-gas-fees)}))))

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
   (let [{:keys [name url]} (-> proposer :metadata)]
     (wallet-connect-core/compute-dapp-name name url))))

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
