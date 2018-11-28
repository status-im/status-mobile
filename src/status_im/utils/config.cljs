(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

;; TODO(oskarth): Extend this to deal with true/false for Jenkins parameter builds
(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.

(def bootnodes-settings-enabled? (enabled? (get-config :BOOTNODES_SETTINGS_ENABLED "1")))
(def rpc-networks-only? (enabled? (get-config :RPC_NETWORKS_ONLY "1")))
(def group-chats-enabled? (enabled? (get-config :GROUP_CHATS_ENABLED "0")))
(defn pairing-enabled? [dev-mode?]
  (and (enabled? (get-config :PAIRING_ENABLED "0"))
       (or dev-mode? platform/desktop?)))
(def mainnet-warning-enabled? (enabled? (get-config :MAINNET_WARNING_ENABLED 0)))
(def pfs-encryption-enabled? (enabled? (get-config :PFS_ENCRYPTION_ENABLED "0")))
(def in-app-notifications-enabled? (enabled? (get-config :IN_APP_NOTIFICATIONS_ENABLED 0)))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def rn-bridge-threshold-warnings-enabled? (enabled? (get-config :RN_BRIDGE_THRESHOLD_WARNINGS 0)))
(def extensions-enabled? (enabled? (get-config :EXTENSIONS 0)))
(def hardwallet-enabled? (enabled? (get-config :HARDWALLET_ENABLED 0)))
(def dev-build? (enabled? (get-config :DEV_BUILD 0)))
(def erc20-contract-warnings-enabled? (enabled? (get-config :ERC20_CONTRACT_WARNINGS)))

;; CONFIG VALUES
(def log-level
  (-> (get-config :LOG_LEVEL "error")
      string/lower-case
      keyword))
(def log-level-status-go
  (-> (get-config :LOG_LEVEL_STATUS_GO "")
      string/upper-case))
(def fleet (get-config :FLEET "eth.beta"))
(def default-network (get-config :DEFAULT_NETWORK))
(def pow-target (js/parseFloat (get-config :POW_TARGET "0.002")))
(def pow-time (js/parseInt (get-config :POW_TIME "1")))
(def use-sym-key (enabled? (get-config :USE_SYM_KEY 0)))
