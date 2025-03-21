(ns status-im.contexts.wallet.networks.chains.mainnet
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id            1
   :related-chain-id    1
   :layer               1
   :short-name          "eth"
   :abbreviated-name    "Eth."
   :full-name           "Mainnet"
   :network-name        :mainnet
   :source              (resources/get-network :mainnet)
   :block-explorer-name "Etherscan"
   :block-explorer-url  "https://etherscan.io/"})

(def sepolia-network
  (assoc network
         :chain-id           11155111
         :testnet?           true
         :related-chain-id   1
         :block-explorer-url "https://sepolia.etherscan.io/"))
