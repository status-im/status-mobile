(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [re-frame.core :as re-frame]
    [re-frame.db :as rf-db]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]
    [tests.integration-test.constants :as integration-constants]
    [utils.security.core :as security]
  ))

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
  (println "DEFAULT ACCOUNT" accounts)
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



;; (deftest wallet-create-derived-addresses-success
;;   (h/log-headline :wallet/create-derived-addresses)
;;   (rf-test/run-test-async
;;    (h/with-app-initialized
;;     (h/with-account
    ;;  (contract-utils/call-rpc-endpoint
    ;;   {:rpc-endpoint "wallet_getDerivedAddresses"
    ;;    :params       [(security/safe-unmask-data "some-password")
    ;;                   [:account :address]
    ;;                   ["m/44'/60'/0'/0/7"]]
;;        :action       get-derived-account})))))


;; (deftest wallet-create-derived-addresses-success
;;   (h/log-headline :wallet/create-derived-addresses)
;;   (rf-test/run-test-async
;;     (h/with-app-initialized
;;       (h/with-account
;;         (let [sha3-pwd            nil
;;               derivation-path     ["m/44'/60'/0'/0/0"]
;;               address             "0xf949b04e6fbb3668c8022ba37c0f5c0f5ff47a9e"
;;               addresses           (contract-utils/call-rpc-endpoint
;;                                   {:rpc-endpoint "wallet_getDerivedAddresses"
;;                                    :params       [sha3-pwd address derivation-path]
;;                                    :action       get-derived-account})]
;;           (println "PASSED" addresses))))))

(defn assert-derived-account
  [response]
  (is (= (security/safe-unmask-data "some-password") (:sha3-pwd response)))
  (is (= "🍌" (:emoji response)))
  (is (= :army (:color response)))
  (is (= "m/44'/60'/0'/0/7" (:path response)))
  (is (= "Test 4" (:account-name response))))

(defn get-main-account
  [accounts]
  (println "ALL ACCOUNTS" accounts)
  (println "MAIN ADDRESS" (:address (first accounts)))
  (:address (first accounts)))

(deftype MaskedData [data]
  Object
    (toString [_] "******"))


(def test-password integration-constants/password)

(defn mask-data
  [data]
  (MaskedData. data))

(def masked-password (mask-data test-password))

(deftest wallet-create-derived-addresses-success
  (h/log-headline :wallet/create-derived-addresses)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (let [sha3-pwd        (security/safe-unmask-data masked-password)
           derivation-path ["m/43'/60'/1581'/0'/0"]
           db-address      (get-in @rf-db/app-db [:profile/profile :address])
           derived         (contract-utils/call-rpc-endpoint
                            {:rpc-endpoint "accounts_getAccounts"
                             :action       get-main-account})
          ]

       (println "PROFILE DB" (get-in @rf-db/app-db [:profile/profile :address]))
       (println "RE-FRAME PROFILE SUBSCRIPTION" @(re-frame/subscribe [:profile/profile :address]))

       (println "DERIVED" derived)

       (println "CONSTANT MAIN ADDRESS" (:main-address integration-constants/recovery-account))
       (println "PASSWORD" integration-constants/password)
       (println "SHA3-PWD" sha3-pwd)

       (contract-utils/call-rpc-endpoint
        {:rpc-endpoint "wallet_getDerivedAddresses"
         :params       [sha3-pwd derived derivation-path]
         :action       assert-derived-account
         :on-error     (fn [error] (println "RPC Call Failed:" error))}))
     (println "FINISHED")))))

