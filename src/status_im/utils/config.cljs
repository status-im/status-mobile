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
(def show-contact-recovery-pop-up? (enabled? (get-config :SHOW_CONTACT_RECOVERY_POPUP)))
(def mailserver-confirmations-enabled? (enabled? (get-config :MAILSERVER_CONFIRMATIONS_ENABLED)))
(def mainnet-warning-enabled? (enabled? (get-config :MAINNET_WARNING_ENABLED 0)))
(def pairing-popup-disabled? (enabled? (get-config :PAIRING_POPUP_DISABLED "0")))
(def pfs-toggle-visible? (enabled? (get-config :PFS_TOGGLE_VISIBLE "0")))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def rn-bridge-threshold-warnings-enabled? (enabled? (get-config :RN_BRIDGE_THRESHOLD_WARNINGS 0)))
(def extensions-enabled? (enabled? (get-config :EXTENSIONS 0)))
(def stickers-enabled? (enabled? (get-config :STICKERS_ENABLED 0)))
(def hardwallet-enabled? (enabled? (get-config :HARDWALLET_ENABLED 0)))
(def dev-build? (enabled? (get-config :DEV_BUILD 0)))
(def erc20-contract-warnings-enabled? (enabled? (get-config :ERC20_CONTRACT_WARNINGS)))
(def partitioned-topic-enabled? (enabled? (get-config :PARTITIONED_TOPIC "0")))
(def tr-to-talk-enabled? (enabled? (get-config :TRIBUTE_TO_TALK 0)))
(def max-message-delivery-attempts (js/parseInt (get-config :MAX_MESSAGE_DELIVERY_ATTEMPTS "6")))
(defn pfs-encryption-enabled? [account]
  (and pfs-toggle-visible?
       (:dev-mode? account)
       (get-in account [:settings :pfs?])))

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
(def max-installations 2)
