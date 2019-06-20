(ns status-im.test.ethereum.core
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.ethereum.core :as ethereum]))

(deftest chain-id->chain-keyword
  (is (= (ethereum/chain-id->chain-keyword 1) :mainnet))
  (is (= (ethereum/chain-id->chain-keyword 3) :testnet))
  (is (= (ethereum/chain-id->chain-keyword 4) :rinkeby))
  (is (= (ethereum/chain-id->chain-keyword 5777) :custom)))

(deftest coordinates
  (is (= {:x "0x46fa4851f3cccd01e3b8d96c130c00bf812502354939eacf06a68fa519ebcbd1"
          :y "0xeb08bebe7403856c0d9686210b9b2e324aa0179747bbba56d53f304a002f31c3"}
         (ethereum/coordinates "0x0446fa4851f3cccd01e3b8d96c130c00bf812502354939eacf06a68fa519ebcbd1eb08bebe7403856c0d9686210b9b2e324aa0179747bbba56d53f304a002f31c3"))))
