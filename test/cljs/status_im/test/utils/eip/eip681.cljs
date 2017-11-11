(ns status-im.test.utils.eip.eip681
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.eip.eip681 :as eip681]
            [status-im.utils.money :as money]))

(deftest parse-uri
  (is (= nil (eip681/parse-uri nil)))
  (is (= nil (eip681/parse-uri 5)))
  (is (= nil (eip681/parse-uri "random")))
  (is (= nil (eip681/parse-uri "ethereum:")))
  (is (= nil (eip681/parse-uri "ethereum:?value=1")))
  (is (= nil (eip681/parse-uri "bitcoin:0x1234")))
  (is (= {:address "0x1234" :chain-id 1} (eip681/parse-uri "ethereum:0x1234")))
  (is (= {:address "0x1234" :value "1" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=1")))
  (is (= {:address "0x1234" :value "-1e18" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=-1e18")))
  (is (= {:address "0x1234" :value "+1E18" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=+1E18")))
  (is (= {:address "0x1234" :value "1E18" :gas "100" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=1E18&gas=100")))
  (is (= {:address "0x1234" :value "NOT_NUMBER" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=NOT_NUMBER")))
  (is (= {:address "0x1234" :value "1ETH" :chain-id 1} (eip681/parse-uri "ethereum:0x1234?value=1ETH")))
  (is (= {:address "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" :value "1e18" :gas "5000" :chain-id 1} (eip681/parse-uri "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7@1?value=1e18&gas=5000")))
  (is (= {:address "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" :value "1e18" :gas "5000" :chain-id 3} (eip681/parse-uri "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7@3?value=1e18&gas=5000")))
  (is (= nil (eip681/parse-uri "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7@2/transfer?value=1e18&gas=5000"))))

(deftest generate-uri
  (is (= nil (eip681/generate-uri nil nil)))
  (is (= "ethereum:0x1234" (eip681/generate-uri "0x1234" nil)))
  (is (= "ethereum:0x1234" (eip681/generate-uri "0x1234" {})))
  (is (= "ethereum:0x1234" (eip681/generate-uri "0x1234" {:value nil})))
  (is (= "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7?value=1" (eip681/generate-uri "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" {:value (money/bignumber 1)})))
  (is (= "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7?value=1000000000000000000" (eip681/generate-uri "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" {:value (money/bignumber 1e18)})))
  (is (= "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7?value=1&gas=100" (eip681/generate-uri "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 1})))
  (is (= "ethereum:0xadaf150b905cf5e6a778e553e15a139b6618bbb7@3?value=1&gas=100" (eip681/generate-uri "0xadaf150b905cf5e6a778e553e15a139b6618bbb7" {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 3})))
  (is (= nil (eip681/generate-uri "0x1234" {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 1 :function-name "transfer"}))))

(deftest parse-value
  (is (= nil (eip681/parse-value nil)))
  (is (= nil (eip681/parse-value 1)))
  (is (= nil (eip681/parse-value {:value "NOT_NUMBER"})))
  (is (= nil (eip681/parse-value {:value "1" :function-name "transfer"})))
  (is (.equals (money/bignumber 1) (eip681/parse-value {:value "1"})))
  (is (.equals (money/bignumber 1e18) (eip681/parse-value {:value "1ETH"})))
  (is (.equals (money/bignumber -1e18) (eip681/parse-value {:value "-1e18"})))
  (is (.equals (money/bignumber 1e18) (eip681/parse-value {:value "1E18"})))
  (is (.equals (money/bignumber "111122223333441239") (eip681/parse-value {:value "111122223333441239"}))))