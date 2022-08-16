(ns status-im.subs-test
  (:require [cljs.test :refer [deftest is testing]]
            status-im.subs))

(def transactions [{:timestamp "1505912551000"}
                   {:timestamp "1505764322000"}
                   {:timestamp "1505750000000"}])

(def grouped-transactions '({:title "20 Sep"
                             :key :20170920
                             :data
                             ({:timestamp "1505912551000"})}
                            {:title "18 Sep"
                             :key :20170918
                             :data
                             ({:timestamp "1505764322000"}
                              {:timestamp "1505750000000"})}))

(deftest group-transactions-by-date
  (testing "Check if transactions are sorted by date"
    (is (= (#'status-im.subs/group-transactions-by-date transactions)
           grouped-transactions))))

(deftest login-ma-keycard-pairing
  (testing "returns nil when no :multiaccounts/login"
    (let [res (status-im.subs/login-ma-keycard-pairing
               {:multiaccounts/login nil
                :multiaccounts/multiaccounts
                {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
               {})]
      (is (nil? res))))

  (testing "returns :keycard-pairing when :multiaccounts/login is present"
    (let [res (status-im.subs/login-ma-keycard-pairing
               {:multiaccounts/login {:key-uid "0x1"}
                :multiaccounts/multiaccounts
                {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
               {})]
      (is (= res "keycard-pairing-code")))))
