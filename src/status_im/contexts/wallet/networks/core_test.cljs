(ns status-im.contexts.wallet.networks.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.networks.core :as sut]))

(def ^:private mainnet
  {:chain-id           1
   :network-name       :mainnet
   :block-explorer-url "https://mainnet.block-explorer/"})
(def ^:private optimism
  {:chain-id           10
   :network-name       :optimism
   :block-explorer-url "https://optimism.block-explorer/"})
(def ^:private sepolia
  {:chain-id           11155111
   :network-name       :mainnet
   :block-explorer-url "https://sepolia.block-explorer/"})
(def ^:private optimism-sepolia
  {:chain-id           11155420
   :network-name       :optimism
   :block-explorer-url "https://optimism-sepolia.block-explorer/"})

(defn- get-db
  ([]
   (get-db {:testnet? false}))
  ([{:keys [testnet?]}]
   {:profile/profile {:test-networks-enabled? testnet?}
    :wallet          {:networks-by-id {1        mainnet
                                       10       optimism
                                       11155111 sepolia
                                       11155420 optimism-sepolia}
                      :networks       {:prod [mainnet optimism]
                                       :test [sepolia optimism-sepolia]}}}))

(deftest get-testnet-mode-key-test
  (testing "returns the :test key correctly"
    (let [db (get-db {:testnet? true})]
      (is (match? :test (sut/get-testnet-mode-key db)))))
  (testing "returns the :prod key correctly"
    (let [db (get-db {:testnet? false})]
      (is (match? :prod (sut/get-testnet-mode-key db))))))

(deftest get-network-details-test
  (testing "returns the network by chain-id corectly, regardless of testnet mode"
    (let [db (get-db)]
      (is (match? mainnet (sut/get-network-details db 1))))))

(deftest get-networks-test
  (testing "returns testnet networks"
    (let [db (get-db {:testnet? true})]
      (is (match? [sepolia optimism-sepolia] (sut/get-networks db)))))
  (testing "returns mainnet networks"
    (let [db (get-db {:testnet? false})]
      (is (match? [mainnet optimism] (sut/get-networks db))))))

(deftest get-chain-ids-test
  (testing "returns testnet chain-ids"
    (let [db (get-db {:testnet? true})]
      (is (match? #{11155111 11155420} (sut/get-chain-ids db)))))
  (testing "returns mainnet chain-ids"
    (let [db (get-db {:testnet? false})]
      (is (match? #{1 10} (sut/get-chain-ids db))))))

(deftest get-block-explorer-address-url-test
  (testing "returns the block-explorer address url"
    (is (match? "https://optimism.block-explorer/address/0x123"
                (sut/get-block-explorer-address-url (get-db) 10 "0x123")))))

(deftest get-block-explorer-tx-url-test
  (testing "returns the block-explorer transaction url"
    (is (match? "https://sepolia.block-explorer/tx/0x123"
                (sut/get-block-explorer-tx-url (get-db) 11155111 "0x123")))))

(deftest eth-mainnet?-test
  (testing "returns true if network is mainnet ethereum"
    (is (match? true (sut/eth-mainnet? mainnet))))
  (testing "returns false if network is not mainnet ethereum"
    (is (match? false (sut/eth-mainnet? optimism)))
    (is (match? false (sut/eth-mainnet? sepolia)))
    (is (match? false (sut/eth-mainnet? optimism-sepolia)))))
