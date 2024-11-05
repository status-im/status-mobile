(ns status-im.contexts.wallet.common.utils.external-links
  (:require [status-im.config :as config]
            [status-im.constants :as constants]))

(defn get-explorer-url-by-chain-id
  [chain-id]
  (cond
    (= chain-id constants/ethereum-mainnet-chain-id)
    config/mainnet-chain-explorer-link

    (= chain-id constants/arbitrum-mainnet-chain-id)
    config/arbitrum-mainnet-chain-explorer-link

    (= chain-id constants/optimism-mainnet-chain-id)
    config/optimism-mainnet-chain-explorer-link

    (= chain-id constants/ethereum-sepolia-chain-id)
    config/sepolia-chain-explorer-link

    (= chain-id constants/arbitrum-sepolia-chain-id)
    config/arbitrum-sepolia-chain-explorer-link

    (= chain-id constants/optimism-sepolia-chain-id)
    config/optimism-sepolia-chain-explorer-link

    :else config/mainnet-chain-explorer-link))
