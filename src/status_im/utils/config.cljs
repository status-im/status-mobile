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
;; STUB_STATUS_GO - indefinite
;; OFFLINE_INBOX_ENABLED - TBD, tenatively until #idea 1 is merged

(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))
(def stub-status-go? (enabled? (get-config :STUB_STATUS_GO 0)))
(def mainnet-networks-enabled? (enabled? (get-config :MAINNET_NETWORKS_ENABLED 0)))
(def offline-inbox-enabled? (enabled? (get-config :OFFLINE_INBOX_ENABLED 0)))
(def offline-inbox-many-enabled? (enabled? (get-config :OFFLINE_INBOX_MANY_ENABLED 0)))
(def log-level
  (-> (get-config :LOG_LEVEL "error")
      string/lower-case
      keyword))

(def jsc-enabled? (enabled? (get-config :JSC_ENABLED 0)))
(def queue-message-enabled? (enabled? (get-config :QUEUE_MESSAGE_ENABLED 0)))
(def many-whisper-topics-enabled? (enabled? (get-config :MANY_WHISPER_TOPICS_ENABLED 0)))
(def rn-bridge-threshold-warnings-enabled? (enabled? (get-config :RN_BRIDGE_THRESHOLD_WARNINGS 0)))
(def compile-views-enabled? (enabled? (get-config :COMPILE_VIEWS_ENABLED 0)))
(def mixpanel-token (get-config :MIXPANEL_TOKEN))
(def default-network (get-config :DEFAULT_NETWORK))

(def pow-target (js/parseFloat (get-config :POW_TARGET "0.001")))
(def pow-time (js/parseInt (get-config :POW_TIME "1")))
