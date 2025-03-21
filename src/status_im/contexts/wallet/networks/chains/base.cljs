(ns status-im.contexts.wallet.networks.chains.base
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id            8453
   :related-chain-id    8453
   :layer               2
   :short-name          "base"
   :abbreviated-name    "Base"
   :full-name           "Base"
   :network-name        :base
   :source              (resources/get-network :base)
   :block-explorer-name "Basescan"
   :block-explorer-url  "https://basescan.org/"})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id           84532
         :testnet?           true
         :related-chain-id   8453
         :block-explorer-url "https://sepolia.basescan.org/"))
