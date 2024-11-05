(ns status-im.contexts.profile.config
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.config :as config]
    [utils.transforms :as transforms]))

(defn login
  []
  {;; Temporary fix until https://github.com/status-im/status-go/issues/3024 is resolved
   :wakuV2Nameserver              "8.8.8.8"
   :statusProxyEnabled            config/status-proxy-enabled?
   :statusProxyStageName          config/status-proxy-stage-name
   :statusProxyMarketUser         config/STATUS_BUILD_PROXY_USER
   :statusProxyMarketPassword     config/STATUS_BUILD_PROXY_PASSWORD
   :statusProxyBlockchainUser     config/STATUS_BUILD_PROXY_USER
   :statusProxyBlockchainPassword config/STATUS_BUILD_PROXY_PASSWORD
   :openseaAPIKey                 config/opensea-api-key
   :poktToken                     config/POKT_TOKEN
   :infuraToken                   config/INFURA_TOKEN
   :raribleMainnetAPIKey          config/RARIBLE_MAINNET_API_KEY
   :raribleTestnetAPIKey          config/RARIBLE_TESTNET_API_KEY
   :alchemyEthereumMainnetToken   config/ALCHEMY_ETHEREUM_MAINNET_TOKEN
   :alchemyEthereumSepoliaToken   config/ALCHEMY_ETHEREUM_SEPOLIA_TOKEN
   :alchemyOptimismMainnetToken   config/ALCHEMY_OPTIMISM_MAINNET_TOKEN
   :alchemyOptimismSepoliaToken   config/ALCHEMY_OPTIMISM_SEPOLIA_TOKEN
   :alchemyArbitrumMainnetToken   config/ALCHEMY_ARBITRUM_MAINNET_TOKEN
   :alchemyArbitrumSepoliaToken   config/ALCHEMY_ARBITRUM_SEPOLIA_TOKEN})

(defn create
  []
  (let [log-enabled? (boolean (not-empty config/log-level))]
    (assoc
     (login)
     :deviceName                                   (native-module/get-installation-name)
     :rootDataDir                                  (native-module/backup-disabled-data-dir)
     :rootKeystoreDir                              (native-module/keystore-dir)
     :logLevel                                     (when log-enabled? config/log-level)
     :logEnabled                                   log-enabled?
     :logFilePath                                  (native-module/log-file-directory)
     :verifyTransactionURL                         config/verify-transaction-url
     :verifyENSURL                                 config/verify-ens-url
     :verifyENSContractAddress                     config/verify-ens-contract-address
     :verifyTransactionChainID                     config/verify-transaction-chain-id
     :wakuV2LightClient                            true
     :wakuV2Fleet                                  config/fleet
     ;; NOTE: https://github.com/status-im/status-go/pull/5570#discussion_r1690794119
     :wakuV2EnableMissingMessageVerification       true
     :wakuV2EnableStoreConfirmationForMessagesSent false
     :previewPrivacy                               config/blank-preview?
     :testNetworksEnabled                          config/test-networks-enabled?)))

(defn strip-file-prefix
  [path]
  (when path
    (string/replace-first path "file://" "")))

(re-frame/reg-event-fx :profile.config/get-node-config-callback
 (fn [{:keys [db]} [node-config-json]]
   (let [node-config (transforms/json->clj node-config-json)]
     {:db (assoc-in db
           [:profile/profile :wakuv2-config]
           (get node-config :WakuV2Config))})))

(re-frame/reg-fx :profile.config/get-node-config
 (fn []
   (native-module/get-node-config
    #(re-frame/dispatch [:profile.config/get-node-config-callback %]))))
