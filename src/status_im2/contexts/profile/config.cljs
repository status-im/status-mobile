(ns status-im2.contexts.profile.config
  (:require [status-im2.config :as config]
            [native-module.core :as native-module]
            [clojure.string :as string]))

(defn login
  []
  {;; Temporary fix until https://github.com/status-im/status-go/issues/3024 is
   ;; resolved
   :wakuV2Nameserver            "1.1.1.1"
   :openseaAPIKey               config/opensea-api-key
   :poktToken                   config/POKT_TOKEN
   :infuraToken                 config/INFURA_TOKEN
   :alchemyOptimismMainnetToken config/ALCHEMY_OPTIMISM_MAINNET_TOKEN
   :alchemyOptimismGoerliToken  config/ALCHEMY_OPTIMISM_GOERLI_TOKEN
   :alchemyArbitrumMainnetToken config/ALCHEMY_ARBITRUM_MAINNET_TOKEN
   :alchemyArbitrumGoerliToken  config/ALCHEMY_ARBITRUM_GOERLI_TOKEN})

(defn create
  []
  (let [log-enabled? (boolean (not-empty config/log-level))]
    (merge (login)
           {:deviceName               (native-module/get-installation-name)
            :backupDisabledDataDir    (native-module/backup-disabled-data-dir)
            :rootKeystoreDir          (native-module/keystore-dir)

            :logLevel                 (when log-enabled? config/log-level)
            :logEnabled               log-enabled?
            :logFilePath              (native-module/log-file-directory)
            :verifyTransactionURL     config/verify-transaction-url
            :verifyENSURL             config/verify-ens-url
            :verifyENSContractAddress config/verify-ens-contract-address
            :verifyTransactionChainID config/verify-transaction-chain-id
            :upstreamConfig           config/default-network-rpc-url
            :networkId                config/default-network-id
            :currentNetwork           config/default-network
            :previewPrivacy           config/blank-preview?})))

(defn strip-file-prefix
  [path]
  (when path
    (string/replace-first path "file://" "")))
