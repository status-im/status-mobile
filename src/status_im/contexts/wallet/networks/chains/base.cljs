(ns status-im.contexts.wallet.networks.chains.base
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id                8453
   :related-chain-id        8453
   :layer                   2
   :short-name              "base"
   :abbreviated-name        "Base"
   :full-name               "Base"
   :network-name            :base
   :source                  (resources/get-network :base)
   :chain-explorer-name     "Basescan"
   :tx-details-base-url     "https://basescan.org/tx"
   :chain-explorer-base-url "https://basescan.org/address/"})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id                84532
         :related-chain-id        8453
         :tx-details-base-url     "https://sepolia.basescan.org/tx"
         :chain-explorer-base-url "https://sepolia.basescan.org/address/"))
