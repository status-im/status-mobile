(ns status-im.contexts.wallet.networks.chains.status
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id            0
   :related-chain-id    0
   :layer               2
   :short-name          "status"
   :abbreviated-name    "Status"
   :full-name           "Status Network"
   :network-name        :status
   :source              (resources/get-network :status)
   :block-explorer-name "Status Explorer"
   :block-explorer-url  ""})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id           1660990954
         :testnet?           true
         :related-chain-id   0
         :block-explorer-url "https://sepoliascan.status.network/"))
