(ns status-im.tribute-to-talk.db
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as dependencies]))

(defn utils [] (dependencies/web3-utils))

(defn to-wei
  [s]
  (when s
    (.toWei (utils) s)))

(defn from-wei
  [s]
  (when s
    (.fromWei (utils) s)))

(defn get-settings
  [db]
  (let [chain-keyword (ethereum/chain-keyword db)]
    (get-in db [:account/account :settings :tribute-to-talk chain-keyword])))

(defn enabled?
  [db]
  (:snt-amount (get-settings db)))
