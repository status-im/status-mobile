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

(defn get-main-account
  [accounts] 
  (:address (first accounts)))

(def derived-address "0x542bf2d18e83ba176c151c949c53eee896fa5a3e")

(defn assert-derived-account
  [response]
  (let [{:keys [address public-key]} (first response)]
    (is (= derived-address address))
    (is (= (:public-key integration-constants/recovery-account) public-key))
    (is (= "m/43'/60'/1581'/0'/0" (:path (first response))))))

(deftest wallet-get-derived-addressess-contract-test
  (h/test-async :wallet/create-derived-addresses
                (fn []
                  (p/let [sha3-pwd        (native-module/sha3 integration-constants/password)
                          derivation-path ["m/43'/60'/1581'/0'/0"]
                          accounts        (contract-utils/call-rpc "accounts_getAccounts")
                          main-account    (get-main-account accounts)
                          response        (contract-utils/call-rpc
                                           "wallet_getDerivedAddresses"
                                           sha3-pwd
                                           main-account
                                           derivation-path)]
                    (assert-derived-account response)))))
