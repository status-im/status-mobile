(ns status-im.contexts.wallet.common.utils.external-links
  (:require [status-im.config :as config]
            [status-im.constants :as constants]))

(defn get-explorer-url-by-chain-id
  [chain-id]
  (condp = chain-id
    constants/ethereum-mainnet-chain-id
    config/mainnet-chain-explorer-link

    constants/arbitrum-mainnet-chain-id
    config/arbitrum-mainnet-chain-explorer-link

    constants/optimism-mainnet-chain-id
    config/optimism-mainnet-chain-explorer-link

    constants/base-mainnet-chain-id
    config/base-mainnet-chain-explorer-link

    constants/ethereum-sepolia-chain-id
    config/sepolia-chain-explorer-link

    constants/arbitrum-sepolia-chain-id
    config/arbitrum-sepolia-chain-explorer-link

    constants/optimism-sepolia-chain-id
    config/optimism-sepolia-chain-explorer-link

    constants/base-sepolia-chain-id
    config/base-sepolia-chain-explorer-link

    config/mainnet-chain-explorer-link))

(defn get-base-url-for-tx-details-by-chain-id
  [chain-id]
  (condp = chain-id
    constants/ethereum-mainnet-chain-id
    config/mainnet-tx-details-base-link

    constants/arbitrum-mainnet-chain-id
    config/arbitrum-mainnet-tx-details-base-link

    constants/optimism-mainnet-chain-id
    config/optimism-mainnet-tx-details-base-link

    constants/base-mainnet-chain-id
    config/base-mainnet-tx-details-base-link

    constants/ethereum-sepolia-chain-id
    config/mainnet-sepolia-tx-details-base-link

    constants/arbitrum-sepolia-chain-id
    config/arbitrum-sepolia-tx-details-base-link

    constants/optimism-sepolia-chain-id
    config/optimism-sepolia-tx-details-base-link

    constants/base-sepolia-chain-id
    config/base-sepolia-tx-details-base-link

    config/mainnet-tx-details-base-link))

(defn get-link-to-tx-details
  [chain-id tx-hash]
  (str (get-base-url-for-tx-details-by-chain-id chain-id) "/" tx-hash))
