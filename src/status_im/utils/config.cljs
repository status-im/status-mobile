(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

(defn enabled? [v] (= "1" v))

;; NOTE(oskarth): Feature flag deprecation lifecycles. We want to make sure
;; flags stay up to date and are removed once behavior introduced is stable.
;;
;; TESTFAIRY_ENABLED - indefinite
;; NOTIFICATIONS_WIP_ENABLED - in 0.9.12 release, remove in develop if all goes well
;; STUB_STATUS_GO - TBD - roman knows
;; NETWORK_SWITCHING - TBD - roman knows

(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))
(def notifications-wip-enabled? (enabled? (get-config :NOTIFICATIONS_WIP_ENABLED 0)))
(def stub-status-go? (enabled? (get-config :STUB_STATUS_GO 0)))
(def network-switching-enabled? (enabled? (get-config :NETWORK_SWITCHING 0)))
(def mainnet-networks-enabled? (enabled? (get-config :MAINNET_NETWORKS_ENABLED 0)))

