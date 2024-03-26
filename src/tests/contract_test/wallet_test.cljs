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

(def number-of-networks 3)

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

(defn check-emoji-is-updated
  [test-emoji accounts]
  (let [default-account (contract-utils/get-default-account accounts)]
    (is (= (:emoji default-account) test-emoji))))

(deftest accounts-save-accounts-contract
  (h/test-async :contract/accounts-save-account
    (fn []
      (p/let [test-emoji      (emoji-picker.utils/random-emoji)
              account         (contract-utils/call-rpc "accounts_getAccounts")
              default-account (contract-utils/get-default-account account)
              _ (contract-utils/call-rpc
                 "accounts_saveAccount"
                 (data-store/<-account (merge default-account {:emoji test-emoji})))
              accounts        (contract-utils/call-rpc "accounts_getAccounts")]
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

(deftest accounts-get-chains-contract
  (h/test-async :contract/wallet_get-ethereum-chains
    (fn []
      (p/let [response (contract-utils/call-rpc "wallet_getEthereumChains")]
        (assert-ethereum-chains response)))))

(defn assert-wallet-tokens
  [tokens]
  (let [flattened-tokens (mapcat val tokens)]
    (doseq [token flattened-tokens]
      (is (not-empty (:symbol token)))
      (is (:decimals token))
      (is (contains? token :balancesPerChain))
      (is (contains? token :marketValuesPerCurrency))
      (is (contains? (:marketValuesPerCurrency token) :usd))
      (let [balances-per-chain (:balancesPerChain token)]
        (doseq [[_ balance] balances-per-chain]
          (is (contains? balance :rawBalance))
          (let [raw-balance (:rawBalance balance)]
            (is (not-empty raw-balance))
            (is (re-matches #"\d+" raw-balance))))))))

(deftest wallet-get-walet-token-test
  (h/test-async :wallet/get-wallet-token
    (fn []
      (p/let [accounts        (contract-utils/call-rpc "accounts_getAccounts")
              default-address (contract-utils/get-default-address accounts)
              response        (contract-utils/call-rpc
                               "wallet_getWalletToken"
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
      (p/let [input       "test.eth"
              chain-id    constants/ethereum-mainnet-chain-id
              ens-address (contract-utils/call-rpc "ens_addressOf" chain-id input)
              response    (contract-utils/call-rpc "wallet_getAddressDetails" chain-id ens-address)]
        (assert-address-details response)))))
