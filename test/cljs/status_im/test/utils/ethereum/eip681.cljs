(ns status-im.test.utils.ethereum.eip681
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.money :as money]))

(deftest parse-uri
  (is (= nil (eip681/parse-uri nil)))
  (is (= nil (eip681/parse-uri 5)))
  (is (= nil (eip681/parse-uri "random")))
  (is (= nil (eip681/parse-uri "ethereum:")))
  (is (= nil (eip681/parse-uri "ethereum:?value=1")))
  (is (= nil (eip681/parse-uri "bitcoin:0x1234")))
  (is (= nil (eip681/parse-uri "ethereum:0x1234")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7", :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?unknown=1")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7", :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "2.014e18" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=2.014e18")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "-1e18" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=-1e18")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "+1E18" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=+1E18")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1E18" :gas "100" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1E18&gas=100")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "NOT_NUMBER" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=NOT_NUMBER")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1ETH" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1ETH")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1e18" :gas "5000" :chain-id 1} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@1?value=1e18&gas=5000")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1e18" :gas "5000" :chain-id 3} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1e18&gas=5000")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1 :function-name "transfer"} (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1 :function-name "transfer" :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052" :uint256 "1"}}
         (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1")))
  (is (= {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1 :function-name "transfer" :gas "100" :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052" :uint256 "1"}}
         (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1&gas=100"))))

(deftest generate-erc20-uri
  (is (= nil (eip681/generate-erc20-uri nil nil)))
  (is (= "ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e/transfer?uint256=5&address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-erc20-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:symbol :SNT :value 5})))
  (is (= "ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e/transfer?uint256=5&address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7&gas=10000&gasPrice=10000"
         (eip681/generate-erc20-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:symbol :SNT :value 5 :gas 10000 :gasPrice 10000})))
  (is (= "ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e/transfer?uint256=5&address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-erc20-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:symbol :SNT :chain-id 1 :value 5})))
  (is (= "ethereum:0xc55cF4B03948D7EBc8b9E8BAD92643703811d162@3/transfer?uint256=5&address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-erc20-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:symbol :STT :chain-id 3 :value 5}))))

(deftest generate-uri
  (is (= nil (eip681/generate-uri nil nil)))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" nil)))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value nil})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value (money/bignumber 1)})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1000000000000000000" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value (money/bignumber 1e18)})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1&gas=100" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 1})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1&gas=100" (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 3})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1&gas=100"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                              {:gas (money/bignumber 100) :chain-id 1 :function-name "transfer" :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052" :uint256 1}}))))

(deftest round-trip
  (let [uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1&gas=100"
        {:keys [address] :as params} (eip681/parse-uri uri)]
    (is (= uri (eip681/generate-uri address (dissoc params :address)))))
  (let [uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3/transfer?uint256=5&address=0xc55cF4B03948D7EBc8b9E8BAD92643703811d162"
        {:keys [address] :as params} (eip681/parse-uri uri)]
    (is (= uri (eip681/generate-uri address (dissoc params :address))))))

(deftest parse-eth-value
  (is (= nil (eip681/parse-eth-value nil)))
  (is (= nil (eip681/parse-eth-value 1)))
  (is (= nil (eip681/parse-eth-value "NOT_NUMBER")))
  (is (.equals (money/bignumber 1) (eip681/parse-eth-value "1")))
  (is (.equals (money/bignumber 2.014e18) (eip681/parse-eth-value "2.014e18")))
  (is (.equals (money/bignumber 1e18) (eip681/parse-eth-value "1ETH")))
  (is (.equals (money/bignumber -1e18) (eip681/parse-eth-value "-1e18")))
  (is (.equals (money/bignumber 1e18) (eip681/parse-eth-value "1E18")))
  (is (.equals (money/bignumber "111122223333441239") (eip681/parse-eth-value "111122223333441239"))))

(deftest extract-request-details
  (let [{:keys [value symbol address]} (eip681/extract-request-details {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1ETH"})]
    (is (.equals (money/ether->wei (money/bignumber 1)) value))
    (is (= :ETH symbol))
    (is (= "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" address)))
  (is (nil? (eip681/extract-request-details {:address "0x744d70fdbe2ba4cf95131626614a1763df805b9e" :chain-id 1 :function-name "unknown"})))
  (let [{:keys [value symbol address]} (eip681/extract-request-details {:address "0x744d70fdbe2ba4cf95131626614a1763df805b9e" :chain-id 1
                                                                        :function-name "transfer" :function-arguments {:uint256 1000 :address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}})]
    (is (.equals (money/bignumber 1000) value))
    (is (= :SNT symbol))
    (is (= "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" address))))