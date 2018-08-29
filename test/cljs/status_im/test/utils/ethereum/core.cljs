(ns status-im.test.utils.ethereum.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.ethereum.core :as ethereum]))

(deftest call-params
  (testing "ERC20 balance-of params"
    (let [contract "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
          address  "0xa7cfd581060ec66414790691681732db249502bd"]
      (is (= (ethereum/call-params contract "balanceOf(address)" address)
             {:to   "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
              :data "0x70a08231000000000000000000000000a7cfd581060ec66414790691681732db249502bd"})))))

(deftest chain-id->chain-keyword
  (is (= (ethereum/chain-id->chain-keyword 1) :mainnet))
  (is (= (ethereum/chain-id->chain-keyword 3) :testnet))
  (is (= (ethereum/chain-id->chain-keyword 4) :rinkeby))
  (is (= (ethereum/chain-id->chain-keyword 5777) :custom)))