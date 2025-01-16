(ns status-im.contexts.wallet.common.utils.networks-test
  (:require
    [cljs.test :refer [are deftest is testing]]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils.networks :as utils]))

(deftest network->chain-id-test
  (testing "network->chain-id function"
    (is (= (utils/network->chain-id {:network :mainnet :testnet-enabled? false})
           constants/ethereum-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :eth :testnet-enabled? true})
           constants/ethereum-sepolia-chain-id))
    (is (= (utils/network->chain-id {:network "optimism" :testnet-enabled? true})
           constants/optimism-sepolia-chain-id))
    (is (= (utils/network->chain-id {:network "oeth" :testnet-enabled? false})
           constants/optimism-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :arb1 :testnet-enabled? false})
           constants/arbitrum-mainnet-chain-id))
    (is (= (utils/network->chain-id {:network :arbitrum :testnet-enabled? true})
           constants/arbitrum-sepolia-chain-id))))

(deftest short-names->network-preference-prefix-test
  (are [expected short-names]
   (= expected (utils/short-names->network-preference-prefix short-names))
   "eth:"           ["eth"]
   "eth:oeth:"      ["eth" "oeth"]
   "eth:oeth:arb1:" ["eth" "oeth" "arb1"]))

(deftest network-preference-prefix->network-names-test
  (are [expected short-names]
   (= expected (utils/network-preference-prefix->network-names short-names))
   (seq [:mainnet])                     "eth"
   (seq [:mainnet :optimism])           "eth:oeth"
   (seq [:mainnet :optimism :arbitrum]) "eth:oeth:arb1"
   (seq [:mainnet :arbitrum])           "eth:sol:arb1"))

(deftest network-names->network-preference-prefix-test
  (are [expected network-names]
   (= expected (utils/network-names->network-preference-prefix network-names))
   "eth:"           [:mainnet]
   "eth:oeth:"      [:mainnet :optimism]
   "eth:oeth:arb1:" [:mainnet :optimism :arbitrum]
   "eth:arb1:"      [:mainnet :polygon :arbitrum]
   ""               []
   ""               nil))

(deftest network-ids->formatted-text-test
  (testing "Empty network-ids should return an empty string"
    (is (= "" (utils/network-ids->formatted-text []))))

  (testing "Single network-id should return the capitalized name of that network"
    (is (= "Mainnet" (utils/network-ids->formatted-text [constants/ethereum-mainnet-chain-id]))))

  (testing "Two network-ids should return a comma-separated string with 'and' for the last item"
    (is (= "Mainnet and Optimism"
           (utils/network-ids->formatted-text [constants/ethereum-mainnet-chain-id
                                               constants/optimism-mainnet-chain-id]))))

  (testing "Multiple network-ids should return a comma-separated string with 'and' for the last item"
    (is (= "Mainnet, Optimism and Arbitrum"
           (utils/network-ids->formatted-text [constants/ethereum-mainnet-chain-id
                                               constants/optimism-mainnet-chain-id
                                               constants/arbitrum-mainnet-chain-id])))))
