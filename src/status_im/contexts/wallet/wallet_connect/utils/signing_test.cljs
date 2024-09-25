(ns status-im.contexts.wallet.wallet-connect.utils.signing-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.wallet-connect.utils.signing :as sut]))

(deftest flatten-data-test
  (testing "correctly returns the fallback when key is not string/keyword/number"
    (is (= (sut/flatten-data {[1 2] "value"})
           [[["unsupported-key"] "value"]])))

  (testing "correctly flattens a simple map"
    (is (= (sut/flatten-data {:zero 0
                              "one" 1
                              2     2})
           [[["zero"] 0]
            [["one"] 1]
            [["2"] 2]])))

  (testing "correctly flattens a simple vec"
    (is (= (sut/flatten-data ["zero" "one"])
           [[["0"] "zero"]
            [["1"] "one"]])))

  (testing "correctly flattens a nested map"
    (is (= (sut/flatten-data {:nested {:child  "child value"
                                       :nested ["child one" "child two"]}
                              :flat   "child value"})
           [[["nested" "child"] "child value"]
            [["nested" "nested" "0"] "child one"]
            [["nested" "nested" "1"] "child two"]
            [["flat"] "child value"]])))

  (testing "correctly flattens a nested vector"
    (is (= (sut/flatten-data [{:flat          1
                               :nested-vector [1 2]
                               :nested-map    {:one 1}}])
           [[["0" "flat"] 1]
            [["0" "nested-vector" "0"] 1]
            [["0" "nested-vector" "1"] 2]
            [["0" "nested-map" "one"] 1]]))))

(deftest flatten-typed-data-test
  (testing "successfully extracts, flattens and formats the typed data"
    (is (= (sut/flatten-typed-data {:domain  {:chain-id 1}
                                    :types   {:Tx [{:name "to"
                                                    :type "address"}]}
                                    :message {:to     {:address "0x"}
                                              :from   {:address "0x"}
                                              :amount "0x"}})
           [["domain: chain-id:" 1]
            ["message: to: address:" "0x"]
            ["message: from: address:" "0x"]
            ["message: amount:" "0x"]]))))
