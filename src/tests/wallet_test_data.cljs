(ns tests.wallet-test-data
  (:require [status-im.contexts.wallet.networks.config :as networks.config]))

(def mainnet-chain-id networks.config/ethereum-chain-id)
(def optimism-chain-id networks.config/optimism-chain-id)
(def arbitrum-chain-id networks.config/arbitrum-chain-id)
(def sepolia-chain-id networks.config/sepolia-chain-id)
(def arbitrum-sepolia-chain-id networks.config/arbitrum-sepolia-chain-id)
(def optimism-sepolia-chain-id networks.config/optimism-sepolia-chain-id)
(def mainnet-name :mainnet)
(def optimism-name :optimism)
(def arbitrum-name :arbitrum)

(def mainnet
  {:test?                    false
   :short-name               "eth"
   :network-name             mainnet-name
   :chain-id                 mainnet-chain-id
   :related-chain-id         mainnet-chain-id
   :abbreviated-name         "Eth."
   :full-name                "Mainnet"
   :block-explorer-url       "https://eth.block-explorer.com/"
   :active?                  true
   :layer                    1
   :native-currency-symbol   "ETH"
   :native-currency-name     "Ether"
   :native-currency-decimals 18})

(def arbitrum
  {:test?                    false
   :short-name               "arb1"
   :network-name             arbitrum-name
   :chain-id                 arbitrum-chain-id
   :related-chain-id         arbitrum-chain-id
   :abbreviated-name         "Arb1."
   :block-explorer-url       "https://arb.block-explorer.com/"
   :full-name                "Arbitrum"
   :active?                  true
   :layer                    2
   :native-currency-symbol   "ETH"
   :native-currency-name     "Ether"
   :native-currency-decimals 18})

(def optimism
  {:test?                    false
   :short-name               "oeth"
   :network-name             optimism-name
   :chain-id                 optimism-chain-id
   :related-chain-id         optimism-chain-id
   :abbreviated-name         "Opt."
   :full-name                "Optimism"
   :block-explorer-url       "https://opt.block-explorer.com/"
   :active?                  true
   :layer                    2
   :native-currency-symbol   "ETH"
   :native-currency-name     "Ether"
   :native-currency-decimals 18})

(def sepolia
  (assoc mainnet
         :test?            true
         :related-chain-id mainnet-chain-id
         :chain-id         sepolia-chain-id))

(def arbitrum-sepolia
  (assoc mainnet
         :test?            true
         :related-chain-id arbitrum-chain-id
         :chain-id         arbitrum-sepolia-chain-id))

(def optimism-sepolia
  (assoc mainnet
         :test?            true
         :related-chain-id optimism-chain-id
         :chain-id         optimism-sepolia-chain-id))

(def chain-ids-by-mode
  {:prod [mainnet-chain-id
          arbitrum-chain-id
          optimism-chain-id]
   :test [sepolia-chain-id
          arbitrum-sepolia-chain-id
          optimism-sepolia-chain-id]})

(def networks-by-id
  {mainnet-chain-id          mainnet
   optimism-chain-id         optimism
   arbitrum-chain-id         arbitrum
   sepolia-chain-id          sepolia
   arbitrum-sepolia-chain-id arbitrum-sepolia
   optimism-sepolia-chain-id optimism-sepolia})

(defn add-networks-to-db
  ([db]
   (add-networks-to-db db identity))
  ([db update-fn]
   (update-in db
              [:wallet]
              #(-> %
                   (assoc :networks/chain-ids-by-mode chain-ids-by-mode)
                   (assoc :networks/by-id (update-vals networks-by-id update-fn))))))

(defn add-active-networks-to-db
  [db active-chain-ids]
  (add-networks-to-db
   db
   (fn [network]
     (if (not (contains? (set active-chain-ids) (:chain-id network)))
       (assoc network :active? false)
       network))))

(defn add-testnet-enabled-to-db
  [db]
  (assoc-in db [:profile/profile :test-networks-enabled?] true))
