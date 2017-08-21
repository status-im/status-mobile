(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

(defn enabled? [v] (= "1" v))

(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))
(def wallet-wip-enabled? (enabled? (get-config :WALLET_WIP_ENABLED 0)))
(def notifications-wip-enabled? (enabled? (get-config :NOTIFICATIONS_WIP_ENABLED 0)))
(def stub-status-go? (enabled? (get-config :STUB_STATUS_GO 0)))
(def network-switching-enabled? (enabled? (get-config :NETWORK_SWITCHING 0)))

