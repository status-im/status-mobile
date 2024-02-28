(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]))

(defn assert-accounts-get-accounts
  [result]
  (is (true? (some :wallet result)))
  (is (true? (some :chat result)))
  (is (= 2 (count result))))

(deftest accounts-get-accounts-contract
  (h/log-headline :contract/accounts-get-accounts)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (contract-utils/call-rpc-endpoint
      {:rpc-endpoint "accounts_getAccounts"
       :action       assert-accounts-get-accounts})
     (h/logout)
     (rf-test/wait-for
       [::logout/logout-method])))))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))

(defn check-emoji-is-updated
  [test-emoji accounts]
  (let [default-account (get-default-account accounts)]
    (is (= (:emoji default-account) test-emoji))))

(deftest accounts-save-accounts-contract
  (h/log-headline :contract/accounts-save-account)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (let [test-emoji (emoji-picker.utils/random-emoji)
           account    (contract-utils/call-rpc-endpoint
                       {:rpc-endpoint "accounts_getAccounts"
                        :action       get-default-account})]
       (contract-utils/call-rpc-endpoint
        {:rpc-endpoint "accounts_saveAccount"
         :action       identity
         :params       [(data-store/<-account (merge account {:emoji test-emoji}))]})
       (contract-utils/call-rpc-endpoint
        {:rpc-endpoint "accounts_getAccounts"
         :action       #(check-emoji-is-updated test-emoji %)})
       (h/logout)
       (rf-test/wait-for
         [::logout/logout-method]))))))

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
  (h/log-headline :contract/wallet_get-ethereum-chains)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (contract-utils/call-rpc-endpoint
      {:rpc-endpoint "wallet_getEthereumChains"
       :action       assert-ethereum-chains})
     (h/logout)
     (rf-test/wait-for
       [::logout/logout-method])))))

(defn get-derived-account
  [response]
  (is (= "0x1" (:sha3-pwd response)))
  (is (= "🍌" (:emoji response)))
  (is (= :army (:color response)))
  (is (= "m/44'/60'/0'/0/7" (:path response)))
  (is (= "Test 3" (:account-name response))))

(deftest wallet-create-derived-addresses-success
  (h/log-headline :wallet/create-derived-addresses)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (contract-utils/call-rpc-endpoint
      {:rpc-endpoint "wallet_getDerivedAccount"
       :params       ["0x1"
                      "some-account-address"
                      ["m/44'/60'/0'/0/7"]]
       :action       get-derived-account})))))
