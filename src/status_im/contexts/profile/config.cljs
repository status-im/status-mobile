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
   :kdfIterations                 3200
   :ethRpcProxyUser               config/STATUS_BUILD_ETH_RPC_PROXY_USER
   :ethRpcProxyPassword           config/STATUS_BUILD_ETH_RPC_PROXY_PASSWORD
   :ethRpcProxyUrl                config/STATUS_BUILD_ETH_RPC_PROXY_URL
   :statusProxyEnabled            config/status-proxy-enabled?
   :statusProxyStageName          config/status-proxy-stage-name
   :statusProxyMarketUser         config/STATUS_BUILD_PROXY_USER
   :statusProxyMarketPassword     config/STATUS_BUILD_PROXY_PASSWORD
   :statusProxyBlockchainUser     config/STATUS_BUILD_PROXY_USER
   :statusProxyBlockchainPassword config/STATUS_BUILD_PROXY_PASSWORD
   :openseaAPIKey                 "foo"
   :poktToken                     "foo"
   :infuraToken                   "foo"
   :raribleMainnetAPIKey          "foo"
   :raribleTestnetAPIKey          "foo"
   :alchemyEthereumMainnetToken   "foo"
   :alchemyEthereumSepoliaToken   "foo"
   :alchemyOptimismMainnetToken   "foo"
   :alchemyOptimismSepoliaToken   "foo"
   :alchemyArbitrumMainnetToken   "foo"
   :alchemyArbitrumSepoliaToken   "foo"
   :alchemyBaseMainnetToken       "foo"
   :alchemyBaseSepoliaToken       "foo"})

(defn- common-config
  []
  {:verifyTransactionURL                         "foo"
   :verifyENSURL                                 "foo"
   :verifyENSContractAddress                     "foo"
   :verifyTransactionChainID                     config/verify-transaction-chain-id
   :wakuV2LightClient                            true
   :wakuV2EnableMissingMessageVerification       true
   :wakuV2EnableStoreConfirmationForMessagesSent false})

(defn fix-node-config-migration
  []
  (common-config))

(defn create
  []
  (let [log-enabled? (boolean (not-empty config/log-level))]
    (merge
     (login)
     (common-config)
     {:deviceName          (native-module/get-installation-name)
      :rootDataDir         (native-module/backup-disabled-data-dir)
      :rootKeystoreDir     (native-module/keystore-dir)
      :logLevel            (when log-enabled? config/log-level)
      :logEnabled          log-enabled?
      :logFilePath         (native-module/log-file-directory)
      :wakuV2Fleet         config/fleet
      :previewPrivacy      config/blank-preview?
      :testNetworksEnabled config/test-networks-enabled?})))

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
