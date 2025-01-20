(ns status-im.contexts.wallet.collectible.utils-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as utils]))

(def token-id "0xT")
(def contract-address "0xC")

(deftest network->chain-id-test
  (testing "get-opensea-collectible-url mainnet"
    (is (= (utils/get-opensea-collectible-url {:chain-id         constants/ethereum-mainnet-chain-id
                                               :contract-address contract-address
                                               :token-id         token-id})
           "https://opensea.io/assets/ethereum/0xC/0xT")))
  (testing "get-opensea-collectible-url mainnet arbitrum"
    (is (= (utils/get-opensea-collectible-url {:chain-id         constants/arbitrum-mainnet-chain-id
                                               :contract-address contract-address
                                               :token-id         token-id})
           "https://opensea.io/assets/arbitrum/0xC/0xT")))

  (testing "get-opensea-collectible-url mainnet optimism"
    (is (= (utils/get-opensea-collectible-url {:chain-id         constants/optimism-mainnet-chain-id
                                               :contract-address contract-address
                                               :token-id         token-id})
           "https://opensea.io/assets/optimism/0xC/0xT")))

  (testing "get-opensea-collectible-url sepolia"
    (is (= (utils/get-opensea-collectible-url {:chain-id constants/ethereum-sepolia-chain-id
                                               :contract-address contract-address
                                               :token-id token-id
                                               :test-networks-enabled? true})
           "https://testnets.opensea.io/assets/sepolia/0xC/0xT")))
  (testing "get-opensea-collectible-url sepolia arbitrum"
    (is (= (utils/get-opensea-collectible-url {:chain-id constants/arbitrum-sepolia-chain-id
                                               :contract-address contract-address
                                               :token-id token-id
                                               :test-networks-enabled? true})
           "https://testnets.opensea.io/assets/arbitrum-sepolia/0xC/0xT")))

  (testing "get-opensea-collectible-url sepolia optimism"
    (is (= (utils/get-opensea-collectible-url {:chain-id constants/optimism-sepolia-chain-id
                                               :contract-address contract-address
                                               :token-id token-id
                                               :test-networks-enabled? true})
           "https://testnets.opensea.io/assets/optimism-sepolia/0xC/0xT"))))

(deftest sort-collectibles-by-name-test
  (testing "Sorts collectibles by name, moving nil or empty names to the end"
    (let [collectibles        [{:collectible-data {:name "Alpha"}}
                               {:collectible-data {:name nil}}
                               {:collectible-data {:name "Beta"}}
                               {:collectible-data {:name ""}}
                               {:collectible-data {:name "Zeta"}}]
          sorted-collectibles (utils/sort-collectibles-by-name collectibles)
          expected            [{:collectible-data {:name "Alpha"}}
                               {:collectible-data {:name "Beta"}}
                               {:collectible-data {:name "Zeta"}}
                               {:collectible-data {:name nil}}
                               {:collectible-data {:name ""}}]]
      (is (= sorted-collectibles expected)))))
