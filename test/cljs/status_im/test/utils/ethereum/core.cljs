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

(deftest coordinates
  (is (nil? (ethereum/coordinates nil)))
  (is (= nil (ethereum/coordinates "dsfdsfg")))
  (is (= {:x "0x6fa4851f3cccd01e3b8d96c130c00bf812502354939eacf06a68fa519ebcbd1", :y "0xeb08bebe7403856c0d9686210b9b2e324aa0179747bbba56d53f304a002f31c3"}
         (ethereum/coordinates "0x046fa4851f3cccd01e3b8d96c130c00bf812502354939eacf06a68fa519ebcbd1feb08bebe7403856c0d9686210b9b2e324aa0179747bbba56d53f304a002f31c3"))))