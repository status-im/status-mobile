(ns status-im.test.utils.ethereum.core
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.ethereum.core :as ethereum]))

(deftest chain-id->chain-keyword
  (is (= (ethereum/chain-id->chain-keyword 1) :mainnet))
  (is (= (ethereum/chain-id->chain-keyword 3) :testnet))
  (is (= (ethereum/chain-id->chain-keyword 4) :rinkeby))
  (is (= (ethereum/chain-id->chain-keyword 5777) :custom)))
