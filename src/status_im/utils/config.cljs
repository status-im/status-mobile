(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

(defn enabled? [v] (= "1" v))

(def testfairy-enabled? (enabled? (get-config :TESTFAIRY_ENABLED)))
(def wallet-tab-enabled? (enabled? (get-config  :WALLET_TAB_ENABLED 0)))