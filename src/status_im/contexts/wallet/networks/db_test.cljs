(ns status-im.contexts.wallet.networks.db-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.networks.db :as sut]
    [status-im.contexts.wallet.networks.test-data :as test-data]))

(deftest get-testnet-mode-key-test
  (testing "returns the :test key correctly"
    (let [db (test-data/get-db {:testnet? true})]
      (is (match? :test (sut/get-testnet-mode-key db)))))
  (testing "returns the :prod key correctly"
    (let [db (test-data/get-db {:testnet? false})]
      (is (match? :prod (sut/get-testnet-mode-key db))))))

(deftest get-network-details-test
  (testing "returns the network by chain-id corectly, regardless of testnet mode"
    (let [db (test-data/get-db)]
      (is (match? test-data/mainnet (sut/get-network-details db 1))))))

(deftest get-networks-test
  (testing "returns testnet networks"
    (let [db (test-data/get-db {:testnet? true})]
      (is (match? [test-data/sepolia test-data/optimism-sepolia] (sut/get-networks db)))))
  (testing "returns mainnet networks"
    (let [db (test-data/get-db {:testnet? false})]
      (is (match? [test-data/mainnet test-data/optimism] (sut/get-networks db))))))

(deftest get-chain-ids-test
  (testing "returns testnet chain-ids"
    (let [db (test-data/get-db {:testnet? true})]
      (is (match? #{11155111 11155420} (sut/get-chain-ids db)))))
  (testing "returns mainnet chain-ids"
    (let [db (test-data/get-db {:testnet? false})]
      (is (match? #{1 10} (sut/get-chain-ids db))))))
