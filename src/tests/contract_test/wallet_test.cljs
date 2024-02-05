(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
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
  (first accounts))

(defn check-emoji-is-updated
  [test-emoji]
  (fn [accounts]
    (let [default-account (first accounts)]
      (is (= (:emoji default-account) test-emoji)))))

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
         :action       (check-emoji-is-updated test-emoji)})
       (h/logout)
       (rf-test/wait-for
         [::logout/logout-method]))))))

(defn assert-ethereum-chains
  [response]
  (is (= 3 (count response)))
  (is (some #(= 1 (get-in % [:Prod :chainId])) response))
  (is (some #(= 10 (get-in % [:Prod :chainId])) response))
  (is (some #(= 42161 (get-in % [:Prod :chainId])) response))
  (is (some #(= 5 (get-in % [:Test :chainId])) response))
  (is (some #(= 421613 (get-in % [:Test :chainId])) response))
  (is (some #(= 420 (get-in % [:Test :chainId])) response)))

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
