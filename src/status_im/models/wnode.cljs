(ns status-im.models.wnode
  (:require
   [clojure.string :as string]
   [status-im.utils.ethereum.core :as ethereum]))

(defn- extract-address-components [address]
  (rest (re-matches #"enode://(.*)@(.*)" address)))

(defn- get-chain [db]
  (let [network  (get (:networks (:account/account db)) (:network db))]
    (ethereum/network->chain-keyword network)))

(defn get-wnode [wnode-id {:keys [db]}]
  (get-in db [:inbox/wnodes (get-chain db) wnode-id]))

(defn current-wnode? [wnode-id {:keys [db]}]
  (let [current-wnode-id (get-in db [:account/account :settings :wnode (get-chain db)])]
    (= current-wnode-id wnode-id)))

(defn build-url [address password]
  (let [[initial host] (extract-address-components address)]
    (str "enode://" initial ":" password "@" host)))
