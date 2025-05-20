(ns status-im.contexts.wallet.networks.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.networks.core :as sut]
    [tests.wallet-test-data :as test-data]))

(deftest eth-mainnet?-test
  (testing "returns true if network is mainnet ethereum"
    (is (match? true (sut/eth-mainnet? test-data/mainnet))))
  (testing "returns false if network is not mainnet ethereum"
    (is (match? false (sut/eth-mainnet? test-data/optimism)))
    (is (match? false (sut/eth-mainnet? test-data/sepolia)))
    (is (match? false (sut/eth-mainnet? test-data/optimism-sepolia)))))

(deftest get-chain-id-test
  (testing "returns the chain-id correctly"
    (is (match? 10 (sut/get-chain-id [test-data/mainnet test-data/optimism] :optimism))))
  (testing "returns nil when the network is not present"
    (is (match? nil (sut/get-chain-id [test-data/mainnet] :optimism)))))

(deftest get-block-explorer-address-url-test
  (testing "returns the block-explorer address url"
    (is (match? "https://opt.block-explorer.com/address/0x123"
                (sut/get-block-explorer-address-url test-data/optimism "0x123")))))

(deftest get-block-explorer-tx-url-test
  (testing "returns the block-explorer transaction url"
    (is (match? "https://eth.block-explorer.com/tx/0x123"
                (sut/get-block-explorer-tx-url test-data/mainnet "0x123")))))
