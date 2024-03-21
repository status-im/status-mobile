(ns tests.contract-test.wallet-recovered-account-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [promesa.core :as p]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]
    [tests.integration-test.constants :as integration-constants]))

(use-fixtures :each (h/fixture-session :recovered-account))

(defn assert-derived-account
  [response]
  (let [{:keys [address public-key]} (first response)]
    (is (= integration-constants/derived-address address))
    (is (= (:public-key integration-constants/recovery-account) public-key))
    (is (= integration-constants/derivation-path (:path (first response))))))

(deftest wallet-get-derived-addressess-contract-test
  (h/test-async :wallet/create-derived-addresses
    (fn []
      (p/let [sha3-pwd-hash   (native-module/sha3 integration-constants/password)
              derivation-path [integration-constants/derivation-path]
              accounts        (contract-utils/call-rpc "accounts_getAccounts")
              default-address (contract-utils/get-default-address accounts)
              response        (contract-utils/call-rpc
                               "wallet_getDerivedAddresses"
                               sha3-pwd-hash
                               default-address
                               derivation-path)]
        (assert-derived-account response)))))
