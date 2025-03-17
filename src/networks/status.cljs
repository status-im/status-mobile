(ns networks.status
  (:require [quo.foundations.resources :as resources]))

(def network
  {:chain-id                0
   :related-chain-id        0
   :layer                   2
   :short-name              "status"
   :abbreviated-name        "Status"
   :full-name               "Status Network"
   :network-name            :status
   :source                  (resources/get-network :status)
   :chain-explorer-name     "Status Explorer"
   :tx-details-base-url     ""
   :chain-explorer-base-url ""})

;; Testnets

(def sepolia-network
  (assoc network
         :chain-id                1660990954
         :related-chain-id        0
         :tx-details-base-url     "https://sepoliascan.status.network/address/"
         :chain-explorer-base-url "https://sepoliascan.status.network/address/"))
