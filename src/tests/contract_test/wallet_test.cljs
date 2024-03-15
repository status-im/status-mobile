(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [promesa.core :as p]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]))

(use-fixtures :each (h/fixture-session))

(defn assert-accounts-get-accounts
  [result]
  (is (true? (some :wallet result)))
  (is (true? (some :chat result)))
  (is (= 2 (count result))))

(deftest accounts-get-accounts-contract-test
  (h/test-async :contract/accounts-get-accounts
    (fn []
      (p/let [result (contract-utils/call-rpc "accounts_getAccounts")]
        (assert-accounts-get-accounts result)))))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))

(defn check-emoji-is-updated
  [test-emoji accounts]
  (let [default-account (get-default-account accounts)]
    (is (= (:emoji default-account) test-emoji))))

(deftest accounts-save-accounts-contract
  (h/test-async :contract/accounts-save-account
    (fn []
      (p/let [test-emoji      (emoji-picker.utils/random-emoji)
              account         (contract-utils/call-rpc "accounts_getAccounts")
              default-account (get-default-account account)
              _ (contract-utils/call-rpc
                 "accounts_saveAccount"
                 (data-store/<-account (merge default-account {:emoji test-emoji})))
              accounts        (contract-utils/call-rpc "accounts_getAccounts")]
        (check-emoji-is-updated test-emoji accounts)))))

(def number-of-networks 3)

(defn assert-ethereum-chains
  [response]
  (is (= number-of-networks (count response)))
  (is (some #(= constants/ethereum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/optimism-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/arbitrum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/ethereum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/arbitrum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/optimism-sepolia-chain-id (get-in % [:Test :chainId])) response)))

(deftest accounts-get-chains-contract
  (h/test-async :contract/wallet_get-ethereum-chains
    (fn []
      (p/let [response (contract-utils/call-rpc "wallet_getEthereumChains")]
        (assert-ethereum-chains response)))))
