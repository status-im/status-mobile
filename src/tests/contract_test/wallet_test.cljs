(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [promesa.core :as promesa]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.json-rpc.events :as rpc-events]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]))

(use-fixtures :each (h/fixture-session))

(def number-of-networks 3)

(defn assert-accounts-get-accounts
  [result]
  (is (true? (some :wallet result)))
  (is (true? (some :chat result)))
  (is (= 2 (count result))))

(deftest accounts-get-accounts-contract-test
  (h/test-async :contract/accounts-get-accounts
    (fn []
      (promesa/let [result (rpc-events/call-async "accounts_getAccounts" false)]
        (assert-accounts-get-accounts result)))))

(defn check-emoji-is-updated
  [test-emoji accounts]
  (let [default-account (contract-utils/get-default-account accounts)]
    (is (= (:emoji default-account) test-emoji))))

(deftest accounts-save-accounts-contract-test
  (h/test-async :contract/accounts-save-account
    (fn []
      (promesa/let [test-emoji      (emoji-picker.utils/random-emoji)
                    account         (rpc-events/call-async "accounts_getAccounts" false)
                    default-account (contract-utils/get-default-account account)
                    _ (rpc-events/call-async
                       "accounts_saveAccount"
                       false
                       (data-store/<-account (merge default-account {:emoji test-emoji})))
                    accounts        (rpc-events/call-async "accounts_getAccounts" false)]
        (check-emoji-is-updated test-emoji accounts)))))

(defn assert-ethereum-chains
  [response]
  (is (= number-of-networks (count response)))
  (is (some #(= constants/ethereum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/optimism-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/arbitrum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/ethereum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/arbitrum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/optimism-sepolia-chain-id (get-in % [:Test :chainId])) response)))

(deftest accounts-get-chains-contract-test
  (h/test-async :contract/wallet_get-ethereum-chains
    (fn []
      (promesa/let [response (rpc-events/call-async "wallet_getEthereumChains" false)]
        (assert-ethereum-chains response)))))

(defn assert-wallet-tokens
  [tokens]
  (let [flattened-tokens (mapcat val tokens)]
    (doseq [token flattened-tokens]
      (is (not-empty (:symbol token)))
      (is (:decimals token))
      (is (contains? token :balancesPerChain))
      (let [balances-per-chain (:balancesPerChain token)]
        (doseq [[_ balance] balances-per-chain]
          (is (contains? balance :rawBalance))
          (let [raw-balance (:rawBalance balance)]
            (is (not-empty raw-balance))
            (is (re-matches #"\d+" raw-balance))))))))

(deftest wallet-get-walet-token-test
  (h/test-async :wallet/get-wallet-token
    (fn []
      (promesa/let [accounts        (rpc-events/call-async "accounts_getAccounts" false)
                    default-address (contract-utils/get-default-address accounts)
                    response        (rpc-events/call-async
                                     "wallet_fetchOrGetCachedWalletBalances"
                                     false
                                     [default-address])]
        (assert-wallet-tokens response)))))

(defn assert-address-details
  [result]
  (is (contains? result :address))
  (is (contains? result :path))
  (is (boolean? (:hasActivity result)))
  (is (false? (:alreadyCreated result))))

(deftest wallet-get-address-details-contract-test
  (h/test-async :wallet/get-address-details
    (fn []
      (promesa/let [input       "test.eth"
                    chain-id    constants/ethereum-mainnet-chain-id
                    ens-address (rpc-events/call-async "ens_addressOf" false chain-id input)
                    response    (rpc-events/call-async "wallet_getAddressDetails"
                                                       false
                                                       chain-id
                                                       ens-address)]
        (assert-address-details response)))))
