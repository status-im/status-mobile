(ns utils.ethereum.eip.eip681-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [utils.ethereum.eip.eip681 :as eip681]
    [utils.money :as money]))

(deftest parse-uri-test
  (is (= nil (eip681/parse-uri nil)))
  (is (= nil (eip681/parse-uri 5)))
  (is (= nil (eip681/parse-uri "random")))
  (is (= nil (eip681/parse-uri "ethereum:")))
  (is (= nil (eip681/parse-uri "ethereum:?value=1")))
  (is (= nil (eip681/parse-uri "bitcoin:0x1234")))
  (is (= nil (eip681/parse-uri "ethereum:0x1234")))
  (is (= nil (eip681/parse-uri "ethereum:gimme.ether?value=1e18")))
  (is (= nil (eip681/parse-uri "ethereum:pay-gimme.ether?value=1e18")))
  (is (= nil
         (eip681/parse-uri
          "ethereum:pay-snt.thetoken.ether/transfer?address=gimme.eth&uint256=1&gas=100")))
  (is (= nil
         (eip681/parse-uri
          "ethereum:pay-snt.thetoken.eth/transfer?address=gimme.ether&uint256=1&gas=100")))

  (is (= (eip681/parse-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"}))

  (is (= (eip681/parse-uri "ethereum:pay-gimme.eth?value=1e18")
         {:address "gimme.eth" :value "1e18" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:pay-0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1e18")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1e18" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?unknown=1")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1}))

  (is
   (=
    (eip681/parse-uri
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?address=0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7")
    {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=2.014e18")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "2.014e18" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=-1e18")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "-1e18" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=+1E18")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "+1E18" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1E18&gas=100")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1E18" :gas "100" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=NOT_NUMBER")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "NOT_NUMBER" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1ETH")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1ETH" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@1?value=1e18&gas=5000")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1e18" :gas "5000" :chain-id 1}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1e18&gas=5000")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :value "1e18" :gas "5000" :chain-id 3}))

  (is (= (eip681/parse-uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer")
         {:address "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" :chain-id 1 :function-name "transfer"}))

  (is
   (=
    (eip681/parse-uri
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1")
    {:address            "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
     :chain-id           1
     :function-name      "transfer"
     :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052" :uint256 "1"}}))

  (is
   (=
    (eip681/parse-uri
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1&gas=100")
    {:address            "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
     :chain-id           1
     :gas                "100"
     :function-name      "transfer"
     :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052" :uint256 "1"}}))

  (is (= (eip681/parse-uri "ethereum:pay-snt.thetoken.eth/transfer?address=gimme.eth&uint256=1&gas=100")
         {:address            "snt.thetoken.eth"
          :chain-id           1
          :gas                "100"
          :function-name      "transfer"
          :function-arguments {:address "gimme.eth" :uint256 "1"}}))

  (is (= (eip681/parse-uri "ethereum:snt.thetoken.eth/transfer?address=gimme.eth&uint256=1&gas=100")
         {:address            "snt.thetoken.eth"
          :chain-id           1
          :gas                "100"
          :function-name      "transfer"
          :function-arguments {:address "gimme.eth" :uint256 "1"}})))

(deftest generate-uri-test
  (is (= nil (eip681/generate-uri nil nil)))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" nil)))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" {:value nil})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                              {:value (money/bignumber 1)})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1000000000000000000"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                              {:value (money/bignumber 1e18)})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7?value=1&gas=100"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                              {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 1})))
  (is (= "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1&gas=100"
         (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                              {:value (money/bignumber 1) :gas (money/bignumber 100) :chain-id 3})))
  (is
   (=
    "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7/transfer?address=0x8e23ee67d1332ad560396262c48ffbb01f93d052&uint256=1&gas=100"
    (eip681/generate-uri "0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
                         {:gas                (money/bignumber 100)
                          :chain-id           1
                          :function-name      "transfer"
                          :function-arguments {:address "0x8e23ee67d1332ad560396262c48ffbb01f93d052"
                                               :uint256 1}}))))

(deftest round-trip-test
  (let [uri "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3?value=1&gas=100"
        {:keys [address] :as params} (eip681/parse-uri uri)]
    (is (= uri (eip681/generate-uri address (dissoc params :address)))))
  (let
    [uri
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7@3/transfer?uint256=5&address=0xc55cF4B03948D7EBc8b9E8BAD92643703811d162"
     {:keys [address] :as params} (eip681/parse-uri uri)]
    (is (= uri (eip681/generate-uri address (dissoc params :address))))))
