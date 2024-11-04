(ns utils.ethereum.chain-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [utils.ethereum.chain :as chain]))

(defn chain-ids-db
  [test-networks-enabled?]
  {:profile/profile {:test-networks-enabled? test-networks-enabled?}
   :wallet          {:networks {:test [{:chain-id 11155111}
                                       {:chain-id 421614}
                                       {:chain-id 11155420}]
                                :prod [{:chain-id 1}
                                       {:chain-id 42161}
                                       {:chain-id 10}]}}})

(deftest chain-id->chain-keyword-test
  (is (= (chain/chain-id->chain-keyword 1) :mainnet))
  (is (= (chain/chain-id->chain-keyword 11155111) :sepolia))
  (is (= (chain/chain-id->chain-keyword 5777) :custom)))

(deftest chain-ids-test
  (is (= (chain/chain-ids (chain-ids-db false)) [1 42161 10]))
  (is (= (chain/chain-ids (chain-ids-db true)) [11155111 421614 11155420])))
