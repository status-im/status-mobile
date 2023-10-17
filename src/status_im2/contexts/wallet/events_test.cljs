(ns status-im2.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im2.contexts.wallet.events :as events]))

(def address "0x2f88d65f3cb52605a54a833ae118fb1363acccd2")

(deftest scan-address-success
  (let [db {}]
    (testing "scan-address-success"
      (let [expected-db {:wallet-2/scanned-address address}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (= result-db expected-db))))))

(deftest clean-scanned-address
  (let [db {:wallet-2/scanned-address address}]
    (testing "clean-scanned-address"
      (let [expected-db {}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (= result-db expected-db))))))
