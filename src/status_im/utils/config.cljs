(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]))

(def config
  (memoize
   (fn []
     (js->clj (.-default rn-dependencies/config) :keywordize-keys true))))

(defn get-config
  ([k] (get (config) k))
  ([k not-found] (get (config) k not-found)))

;; TODO(oskarth): Extend this to deal with true/false for Jenkins parameter builds
(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.

(def bootnodes-settings-enabled? (enabled? (get-config :BOOTNODES_SETTINGS_ENABLED "1")))
(def rpc-networks-only? (enabled? (get-config :RPC_NETWORKS_ONLY "1")))
(def mailserver-confirmations-enabled? (enabled? (get-config :MAILSERVER_CONFIRMATIONS_ENABLED)))
(def pairing-popup-disabled? (enabled? (get-config :PAIRING_POPUP_DISABLED "0")))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def snoopy-enabled? (enabled? (get-config :SNOOPY 0)))
(def hardwallet-enabled? (enabled? (get-config :HARDWALLET_ENABLED 0)))
(def dev-build? (enabled? (get-config :DEV_BUILD 0)))
(def erc20-contract-warnings-enabled? (enabled? (get-config :ERC20_CONTRACT_WARNINGS)))
(def tr-to-talk-enabled? (enabled? (get-config :TRIBUTE_TO_TALK 0)))
(def max-message-delivery-attempts (js/parseInt (get-config :MAX_MESSAGE_DELIVERY_ATTEMPTS "6")))
(def contract-nodes-enabled? (enabled? (get-config :CONTRACT_NODES "0")))
(def mobile-ui-for-desktop? (enabled? (get-config :MOBILE_UI_FOR_DESKTOP "0")))
;; NOTE: only disabled in releases
(def local-notifications? (enabled? (get-config :LOCAL_NOTIFICATIONS "1")))
(def blank-preview? (enabled? (get-config :BLANK_PREVIEW "1")))
(def group-chat-enabled? (enabled? (get-config :GROUP_CHATS_ENABLED "0")))
(def tooltip-events? (enabled? (get-config :TOOLTIP_EVENTS "0")))
(def nimbus-enabled? (enabled? (get-config :STATUS_GO_ENABLE_NIMBUS "0")))
(def waku-enabled? (enabled? (get-config :WAKU_ENABLED "0")))

;; CONFIG VALUES
(def log-level
  (-> (get-config :LOG_LEVEL "error")
      string/lower-case
      keyword))
(def log-level-status-go
  (-> (get-config :LOG_LEVEL_STATUS_GO "")
      string/upper-case))
(def fleet (get-config :FLEET "eth.staging"))
(def default-network (get-config :DEFAULT_NETWORK))
(def pow-target (js/parseFloat (get-config :POW_TARGET "0.002")))
(def pow-time (js/parseInt (get-config :POW_TIME "1")))
(def max-installations 2)
