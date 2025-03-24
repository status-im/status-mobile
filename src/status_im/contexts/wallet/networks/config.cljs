(ns status-im.contexts.wallet.networks.config
  (:require
    [malli.core]
    [malli.error]
    [quo.foundations.resources :as resources]
    [status-im.contexts.wallet.networks.validation :as validation]))

(def ethereum-chain-id 1)
(def sepolia-chain-id 11155111)

(def arbitrum-chain-id 42161)
(def arbitrum-sepolia-chain-id 421614)

(def optimism-chain-id 10)
(def optimism-sepolia-chain-id 11155420)

(def base-chain-id 8453)
(def base-sepolia-chain-id 84532)

(def status-sepolia-chain-id 1660990954)

(def new-networks
  #{base-chain-id
    base-sepolia-chain-id
    status-sepolia-chain-id})

(def mainnets
  {ethereum-chain-id
   {:network-name        :mainnet
    :source              (resources/get-network :mainnet)
    :abbreviated-name    "Eth."
    :block-explorer-name "Etherscan"}

   arbitrum-chain-id
   {:network-name        :arbitrum
    :source              (resources/get-network :arbitrum)
    :abbreviated-name    "Arb1."
    :block-explorer-name "Arbiscan"}

   optimism-chain-id
   {:network-name        :optimism
    :source              (resources/get-network :optimism)
    :abbreviated-name    "Oeth."
    :block-explorer-name "Optimistic"}

   base-chain-id
   {:network-name        :base
    :source              (resources/get-network :base)
    :abbreviated-name    "Base"
    :block-explorer-name "Basescan"}})

(def testnets
  {sepolia-chain-id
   (get mainnets ethereum-chain-id)

   arbitrum-sepolia-chain-id
   (get mainnets arbitrum-chain-id)

   optimism-sepolia-chain-id
   (get mainnets optimism-chain-id)

   base-sepolia-chain-id
   (get mainnets base-chain-id)

   status-sepolia-chain-id
   {:network-name        :status
    :source              (resources/get-network :status)
    :abbreviated-name    "Stat."
    :block-explorer-name "Status Explorer"}})

(def networks
  (merge mainnets testnets))

;; NOTE: runs schema validation over all the networks only in debug
;; mode to make sure the networks are defined correctly
(when ^boolean js/goog.DEBUG
  (map validation/validate-network networks))
