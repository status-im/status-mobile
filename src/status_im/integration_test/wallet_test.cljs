(ns status-im.integration-test.wallet-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [re-frame.core :as rf]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
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
     (rf/dispatch [:integration-test/dispatch-rpc
                   {:rpc-endpoint "accounts_getAccounts"
                    :check-result assert-accounts-contract}])
     (rf-test/wait-for
       [:integration-test/rpc-checked]
       (h/logout)
       (rf-test/wait-for [::logout/logout-method]))))))
