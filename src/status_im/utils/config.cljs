(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

;; TODO(oskarth): Extend this to deal with true/false for Jenkins parameter builds
(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.
;;
;; TESTFAIRY_ENABLED - indefinite

;; CONFIG FLAGS
(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))

(def bootnodes-settings-enabled? (enabled? (get-config :BOOTNODES_SETTINGS_ENABLED "1")))
(def rpc-networks-only? (enabled? (get-config :RPC_NETWORKS_ONLY "1")))
(def group-chats-enabled? (enabled? (get-config :GROUP_CHATS_ENABLED)))
(def mainnet-warning-enabled? (enabled? (get-config :MAINNET_WARNING_ENABLED 0)))
(def in-app-notifications-enabled? (enabled? (get-config :IN_APP_NOTIFICATIONS_ENABLED 0)))
(def cached-webviews-enabled? (enabled? (get-config :CACHED_WEBVIEWS_ENABLED 0)))
(def rn-bridge-threshold-warnings-enabled? (enabled? (get-config :RN_BRIDGE_THRESHOLD_WARNINGS 0)))
(def extensions-enabled? (enabled? (get-config :EXTENSIONS 0)))

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
;; the default value should be a string for `enabled?` to work correctly.
(def testfairy-token (get-config :TESTFAIRY_TOKEN))
(def instabug-token (get-config :INSTABUG_TOKEN))
(def pow-target (js/parseFloat (get-config :POW_TARGET "0.002")))
(def pow-time (js/parseInt (get-config :POW_TIME "1")))
(def use-sym-key (enabled? (get-config :USE_SYM_KEY 0)))
