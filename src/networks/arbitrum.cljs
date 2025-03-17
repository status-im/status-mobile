(ns networks.arbitrum
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id                42161
   :related-chain-id        42161
   :layer                   2
   :short-name              "arb1"
   :abbreviated-name        "Arb1."
   :full-name               "Arbitrum"
   :network-name            :arbitrum
   :source                  (resources/get-network :arbitrum)
   :chain-explorer-name     "Arbiscan"
   :tx-details-base-url     "https://arbiscan.io/tx"
   :chain-explorer-base-url "https://arbiscan.io/address/"})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id                421614
         :related-chain-id        42161
         :tx-details-base-url     "https://sepolia.arbiscan.io/tx"
         :chain-explorer-base-url "https://sepolia.arbiscan.io/address/"))
