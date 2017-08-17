(ns status-im.test.utils.erc20
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.erc20 :as erc20]))

(deftest erc20
  (testing "ERC20 balance-of params"
    (let [contract "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
          address "0xa7cfd581060ec66414790691681732db249502bd"]
      (is (= (erc20/balance-of-params contract address)
             {:to "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
              :data "0x70a08231000000000000000000000000a7cfd581060ec66414790691681732db249502bd"})))))
