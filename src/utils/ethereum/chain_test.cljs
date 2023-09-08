(ns utils.ethereum.chain-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [utils.ethereum.chain :as chain]))

(deftest chain-id->chain-keyword
  (is (= (chain/chain-id->chain-keyword 1) :mainnet))
  (is (= (chain/chain-id->chain-keyword 5) :goerli))
  (is (= (chain/chain-id->chain-keyword 5777) :custom)))
