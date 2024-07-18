(ns status-im.contexts.wallet.common.utils.address-test
  (:require
   [cljs.test :refer-macros [are deftest is testing]]
   [status-im.contexts.wallet.common.utils.address :as utils]
   [status-im.constants :as constants]))

"ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"
"oeth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"

(re-matches constants/regx-multichain-address "oeth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2")

(re-matches constants/regx-metamask-address "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1")
(re-find constants/regx-metamask-address "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1")


#_(deftest network->chain-id-test
    (testing "network->chain-id function"
      (is (= (utils/network->chain-id {:network :mainnet :testnet-enabled? false :goerli-enabled? false})
             constants/ethereum-mainnet-chain-id))
      (is (= (utils/network->chain-id {:network :eth :testnet-enabled? true :goerli-enabled? false})
             constants/ethereum-sepolia-chain-id))
      (is (= (utils/network->chain-id {:network "optimism" :testnet-enabled? true :goerli-enabled? false})
             constants/optimism-sepolia-chain-id))
      (is (= (utils/network->chain-id {:network "oeth" :testnet-enabled? false :goerli-enabled? true})
             constants/optimism-mainnet-chain-id))
      (is (= (utils/network->chain-id {:network :oeth :testnet-enabled? true :goerli-enabled? true})
             constants/optimism-goerli-chain-id))
      (is (= (utils/network->chain-id {:network :arb1 :testnet-enabled? false :goerli-enabled? false})
             constants/arbitrum-mainnet-chain-id))
      (is (= (utils/network->chain-id {:network :arbitrum :testnet-enabled? true :goerli-enabled? false})
             constants/arbitrum-sepolia-chain-id))))

#_(deftest short-names->network-preference-prefix-test
  (are [expected short-names]
   (= expected (utils/short-names->network-preference-prefix short-names))
   "eth:"           ["eth"]
   "eth:oeth:"      ["eth" "oeth"]
   "eth:oeth:arb1:" ["eth" "oeth" "arb1"]))


