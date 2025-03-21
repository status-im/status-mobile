(ns status-im.contexts.wallet.networks.chains.arbitrum
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id            42161
   :related-chain-id    42161
   :layer               2
   :short-name          "arb1"
   :abbreviated-name    "Arb1."
   :full-name           "Arbitrum"
   :network-name        :arbitrum
   :source              (resources/get-network :arbitrum)
   :block-explorer-name "Arbiscan"
   :block-explorer-url  "https://arbiscan.io/"})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id           421614
         :testnet?           true
         :related-chain-id   42161
         :block-explorer-url "https://sepolia.arbiscan.io/"))
