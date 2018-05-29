(ns status-im.test.transport.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.protocol.handlers :as protocol.handlers]
            [status-im.transport.core :as transport]))

(deftest init-whisper
  (let [cofx {:db {:account/account {:public-key "1"}}}]
    (testing "it adds the discover filter"
      (is (= (:shh/add-discovery-filter (protocol.handlers/initialize-protocol cofx [])))))
    (testing "it restores the sym-keys"
      (is (= (:shh/restore-sym-keys (protocol.handlers/initialize-protocol cofx [])))))
    (testing "custom mailservers"
      (let [ms-1            {:id "1"
                             :chain "mainnet"
                             :name "name-1"
                             :address "address-1"
                             :password "password-1"}
            ms-2            {:id "2"
                             :chain "mainnet"
                             :name "name-2"
                             :address "address-2"
                             :password "password-2"}
            ms-3            {:id "3"
                             :chain "rinkeby"
                             :name "name-3"
                             :address "address-3"
                             :password "password-3"}
            expected-wnodes {:mainnet {"1" (-> ms-1
                                               (dissoc :chain)
                                               (assoc :user-defined true))
                                       "2" (-> ms-2
                                               (dissoc ms-2 :chain)
                                               (assoc :user-defined true))}
                             :rinkeby {"3" (-> ms-3
                                               (dissoc :chain)
                                               (assoc :user-defined true))}}
            cofx-with-ms    (assoc cofx
                                   :data-store/mailservers
                                   [ms-1
                                    ms-2
                                    ms-3])]
        (is (= expected-wnodes
               (get-in (protocol.handlers/initialize-protocol cofx-with-ms []) [:db :inbox/wnodes])))))))
