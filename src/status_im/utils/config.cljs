(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.
;;
;; TESTFAIRY_ENABLED - indefinite
;; STUB_STATUS_GO - indefinite
;; NOTIFICATIONS_WIP_ENABLED - in 0.9.12 release, remove in develop if all goes well
;; ERC20_ENABLED - until idea #3 is merged, remove in develop when ready

(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))
(def notifications-wip-enabled? (enabled? (get-config :NOTIFICATIONS_WIP_ENABLED 0)))
(def stub-status-go? (enabled? (get-config :STUB_STATUS_GO 0)))
(def mainnet-networks-enabled? (enabled? (get-config :MAINNET_NETWORKS_ENABLED 0)))
(def erc20-enabled? (enabled? (get-config :ERC20_ENABLED 0)))
(def log-level
  (-> (get-config :LOG_LEVEL "error")
      string/lower-case
      keyword))

;; NOTE(oskarth): status-go perf test delay, not used in status-react:
;; FEATURE_SYNC_DELAY=1000 (in microseconds)

