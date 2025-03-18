(ns status-im.contexts.wallet.networks.chains.mainnet
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id                1
   :related-chain-id        1
   :layer                   1
   :short-name              "eth"
   :abbreviated-name        "Eth."
   :full-name               "Mainnet"
   :network-name            :mainnet
   :source                  (resources/get-network :mainnet)
   :chain-explorer-name     "Etherscan"
   :tx-details-base-url     "https://etherscan.io/tx"
   :chain-explorer-base-url "https://etherscan.io/address/"})

(def sepolia-network
  (assoc network
         :chain-id                        11155111
         :related-chain-id                1
         :sepolia-tx-details-base-url     "https://sepolia.etherscan.io/tx"
         :sepolia-chain-explorer-base-url "https://sepolia.etherscan.io/address/"))
