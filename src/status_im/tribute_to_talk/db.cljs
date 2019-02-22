(ns status-im.tribute-to-talk.db
  (:require [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.core :as ethereum]))

(def utils dependencies/web3-utils)

(defn to-wei
  [s]
  (when s
    (.toWei utils s)))

(defn from-wei
  [s]
  (when s
    (.fromWei utils s)))

(defn get-settings
  [db]
  (let [chain-keyword    (-> (get-in db [:account/account :networks (:network db)])
                             ethereum/network->chain-keyword)]
    (get-in db [:account/account :settings :tribute-to-talk chain-keyword])))

(defn enabled?
  [db]
  (:snt-amount (get-settings db)))
