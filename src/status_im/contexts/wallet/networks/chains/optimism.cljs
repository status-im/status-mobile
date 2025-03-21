(ns status-im.contexts.wallet.networks.chains.optimism
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id            10
   :related-chain-id    10
   :layer               2
   :short-name          "oeth"
   :abbreviated-name    "Oeth."
   :full-name           "Optimism"
   :network-name        :optimism
   :source              (resources/get-network :optimism)
   :block-explorer-name "Optimistic"
   :block-explorer-url  "https://optimistic.etherscan.io/"})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id           11155420
         :testnet?           true
         :related-chain-id   10
         :block-explorer-url "https://sepolia-optimistic.etherscan.io/"))
