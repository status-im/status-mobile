(ns status-im.contexts.wallet.networks.db-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.networks.db :as sut]
    [tests.wallet-test-data :as test-data]))

(deftest get-testnet-mode-key-test
  (testing "returns the :test key correctly"
    (let [db (-> {}
                 (test-data/add-networks-to-db)
                 (test-data/add-testnet-enabled-to-db))]
      (is (match? :test (sut/get-testnet-mode-key db)))))
  (testing "returns the :prod key correctly"
    (let [db (test-data/add-networks-to-db {})]
      (is (match? :prod (sut/get-testnet-mode-key db))))))

(deftest get-network-details-test
  (testing "returns the network by chain-id corectly, regardless of testnet mode"
    (let [db (test-data/add-networks-to-db {})]
      (is (match? test-data/mainnet (sut/get-network-details db 1))))))

(deftest get-networks-test
  (testing "returns testnet networks"
    (let [db (-> {}
                 (test-data/add-networks-to-db)
                 (test-data/add-testnet-enabled-to-db))]
      (is (match? [test-data/sepolia test-data/arbitrum-sepolia test-data/optimism-sepolia]
                  (sut/get-networks db)))))
  (testing "returns mainnet networks"
    (let [db (test-data/add-networks-to-db {})]
      (is (match? [test-data/mainnet test-data/arbitrum test-data/optimism] (sut/get-networks db))))))

(deftest get-chain-ids-test
  (testing "returns testnet chain-ids"
    (let [db (-> {}
                 (test-data/add-networks-to-db)
                 (test-data/add-testnet-enabled-to-db))]
      (is (match? #{test-data/sepolia-chain-id
                    test-data/arbitrum-sepolia-chain-id
                    test-data/optimism-sepolia-chain-id}
                  (sut/get-chain-ids db)))))
  (testing "returns mainnet chain-ids"
    (let [db (test-data/add-networks-to-db {})]
      (is (match? #{test-data/mainnet-chain-id
                    test-data/arbitrum-chain-id
                    test-data/optimism-chain-id}
                  (sut/get-chain-ids db))))))
