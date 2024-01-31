(ns status-im.tests.contract-test.wallet-test
  (:require
   [cljs.test :refer [deftest is]]
   [day8.re-frame.test :as rf-test]
   legacy.status-im.events
   [legacy.status-im.multiaccounts.logout.core :as logout]
   legacy.status-im.subs.root
   status-im.events
   status-im.navigation.core
   status-im.subs.root
   [status-im.tests.contract-test.utils :as contract-utils]
   [test-helpers.integration :as h]))

(defn assert-accounts-contract
  [result]
  (is (true? (some :wallet result)))
  (is (true? (some :chat result)))
  (is (= 2 (count result))))

(deftest check-wallet-accounts-contract
  (h/log-headline :create-wallet-account-test)
  (rf-test/run-test-async
   (h/with-app-initialized
     (h/with-account
       (contract-utils/call-rpc-endpoint
        {:rpc-endpoint "accounts_getAccounts"
         :check-result assert-accounts-contract}
        (fn []
          (h/logout)
          (rf-test/wait-for [::logout/logout-method])))))))
