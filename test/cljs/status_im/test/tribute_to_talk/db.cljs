(ns status-im.test.tribute-to-talk.db
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.tribute-to-talk.db :as db]))

(deftest tribute-status
  (is (= (db/tribute-status {:system-tags #{:tribute-to-talk/paid}
                             :tribute-to-talk {:snt-amount 1000
                                               :transaction-hash "0x"}})
         :paid))
  (is (= (db/tribute-status {:tribute-to-talk {:snt-amount 1000
                                               :transaction-hash "0x"}})
         :pending))
  (is (= (db/tribute-status {:tribute-to-talk {:snt-amount 1000}})
         :required))
  (is (= (db/tribute-status {:tribute-to-talk {:snt-amount 0}})
         :none))
  (is (= (db/tribute-status {})
         :none)))

(deftest valid?
  (is (db/valid? {:snt-amount "1000"}))
  (is (not (db/valid? {:snt-amount "-1000"})))
  (is (not (db/valid? {:snt-amount "1000001000000000000000000"})))
  (is (not (db/valid? {:snt-amount 5})))
  (is (not (db/valid? {:snt-amount "abcdef"}))))

(def sender-pk "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97")
(def sender-address "0xdff1a5e4e57d9723b3294e0f4413372e3ea9a8ff")

(deftest valid-tribute-transaction?
  (testing "a valid transaction"
    (is (db/valid-tribute-transaction?
         {:wallet {:transactions
                   {"transaction-hash-1"
                    {:value "1000000000000000000"
                     :block "5"
                     :from sender-address}}}
          :ethereum/current-block 8}
         "1000000000000000000"
         "transaction-hash-1"
         sender-pk)))
  (testing "a transaction"
    (testing "with insufficient value transfered"
      (is (not (db/valid-tribute-transaction?
                {:wallet {:transactions
                          {"transaction-hash-1"
                           {:value "1"
                            :block "5"
                            :from sender-address}}}
                 :ethereum/current-block 8}
                "1000000000000000000"
                "transaction-hash-1"
                sender-pk)))
      (testing "that was not confirmed yet"
        (is (not (db/valid-tribute-transaction?
                  {:wallet {:transactions
                            {"transaction-hash-1"
                             {:value "1000000000000000000"
                              :block "8"
                              :from sender-address}}}
                   :ethereum/current-block 8}
                  "1000000000000000000"
                  "transaction-hash-1"
                  sender-pk))))
      (testing "from someone else"
        (is (not (db/valid-tribute-transaction?
                  {:wallet {:transactions
                            {"transaction-hash-1"
                             {:value "1000000000000000000"
                              :block "5"
                              :from "another address"}}}
                   :ethereum/current-block 8}
                  "1000000000000000000"
                  "transaction-hash-1"
                  sender-pk))))))
  (testing "a transaction that does not exist"
    (is (not (db/valid-tribute-transaction?
              {:ethereum/current-block 8}
              "1000000000000000000"
              "transaction-hash-1"
              sender-pk)))))
