(ns status-im.contexts.wallet.networks.config
  (:require
    [malli.core]
    [malli.error]
    [quo.foundations.resources :as resources]
    [status-im.contexts.wallet.networks.validation :as validation]))

;; NOTE: this value comes from status-go, as it supports up to 5 networks simultaneously.
(def ^:const max-active-networks 5)

;; NOTE: Add chain-ids below

(def ^:const ethereum-chain-id 1)
(def ^:const sepolia-chain-id 11155111)

;; NOTE: Used for placing the ethereum chains (including testnet) in the top of the list during sorting
(def ^:const ethereum-chain-ids #{ethereum-chain-id sepolia-chain-id})

(def ^:const arbitrum-chain-id 42161)
(def ^:const arbitrum-sepolia-chain-id 421614)

(def ^:const optimism-chain-id 10)
(def ^:const optimism-sepolia-chain-id 11155420)

(def ^:const base-chain-id 8453)
(def ^:const base-sepolia-chain-id 84532)

(def ^:const status-sepolia-chain-id 1660990954)

(def ^:const bsc-chain-id 56)
(def ^:const bsc-testnet-chain-id 97)

;; NOTE: Add a chain to `new-networks` to:
;;         1. highlight it as "new" in the UI
;;         2. add a "new feature" dot to places where we show networks

(def ^:const new-networks
  #{status-sepolia-chain-id
    bsc-chain-id
    bsc-testnet-chain-id})

(def ^:const chain-id-for-new-network-banner bsc-chain-id)

;; NOTE: if the network should be supported in Bridge (Hop), add
;; the chain id to the following set. Otherwise, remove if no longer
;; supported.

(def ^:const bridge-supported-networks
  #{ethereum-chain-id
    arbitrum-chain-id
    optimism-chain-id
    base-chain-id})

;; NOTE: add client-side chain details below for `mainnet` and `testnet`
;; respectively.

(def ^:const mainnets
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
    :block-explorer-name "Basescan"}

   bsc-chain-id
   {:network-name        :bsc
    :source              (resources/get-network :bsc)
    :abbreviated-name    "BSC"
    :block-explorer-name "Bscscan"}})

(def ^:const testnets
  {sepolia-chain-id
   (get mainnets ethereum-chain-id)

   arbitrum-sepolia-chain-id
   (get mainnets arbitrum-chain-id)

   optimism-sepolia-chain-id
   (get mainnets optimism-chain-id)

   base-sepolia-chain-id
   (get mainnets base-chain-id)

   bsc-testnet-chain-id
   (get mainnets bsc-chain-id)

   status-sepolia-chain-id
   {:network-name        :status
    :source              (resources/get-network :status)
    :abbreviated-name    "Stat."
    :block-explorer-name "Status Explorer"}})

(def ^:const networks
  (merge mainnets testnets))

;; NOTE: runs schema validation over all the networks only in debug
;; mode to make sure the networks are defined correctly

(when ^boolean js/goog.DEBUG
  (map validation/validate-network networks))
