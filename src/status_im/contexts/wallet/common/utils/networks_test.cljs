(ns status-im.contexts.wallet.common.utils.networks-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils.networks :as utils]))

(deftest test-network->chain-id
  (testing "network->chain-id function"
    (is (= (utils/network->chain-id {:network :mainnet :testnet-enabled? false :goerli-enabled? false})
           constants/ethereum-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :eth :testnet-enabled? true :goerli-enabled? false})
           constants/ethereum-sepolia-chain-id))
    (is (= (utils/network->chain-id {:network "optimism" :testnet-enabled? true :goerli-enabled? false})
           constants/optimism-sepolia-chain-id))
    (is (= (utils/network->chain-id {:network "opt" :testnet-enabled? false :goerli-enabled? true})
           constants/optimism-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :opt :testnet-enabled? true :goerli-enabled? true})
           constants/optimism-goerli-chain-id))
    (is (= (utils/network->chain-id {:network :arb1 :testnet-enabled? false :goerli-enabled? false})
           constants/arbitrum-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :arbitrum :testnet-enabled? true :goerli-enabled? false})
           constants/arbitrum-sepolia-chain-id))))

(deftest test-short-names->network-preference-prefix
  (testing "short-names->network-preference-prefix function"
    (is (= (utils/short-names->network-preference-prefix ["eth"])
           "eth:"))
    (is (= (utils/short-names->network-preference-prefix ["eth" "opt"])
           "eth:opt:"))
    (is (= (utils/short-names->network-preference-prefix ["eth" "opt" "arb1"])
           "eth:opt:arb1:"))))

(deftest test-network-preference-prefix->network-names
  (testing "network-preference-prefix->network-names function"
    (is (= (utils/network-preference-prefix->network-names "eth")
           ["ethereum"]))
    (is (= (utils/network-preference-prefix->network-names "eth:opt")
           ["ethereum" "optimism"]))
    (is (= (utils/network-preference-prefix->network-names "eth:opt:arb1")
           ["ethereum" "optimism" "arbitrum"]))))
