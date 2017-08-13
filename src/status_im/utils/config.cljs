(ns status-im.utils.config
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def config (js->clj (.-default rn-dependencies/config) :keywordize-keys true))

(defn get-config [k]
  (get config k))

(def testfairy-enabled? (= "1" (get-config :TESTFAIRY_ENABLED)))
