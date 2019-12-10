(ns status-im.data-store.settings
  (:require [status-im.ethereum.eip55 :as eip55]))

(defn rpc->networks [networks]
  (reduce (fn [acc {:keys [id] :as network}]
            (assoc acc id network))
          {}
          networks))

(defn rpc->visible-tokens [visible-tokens]
  (reduce-kv (fn [acc chain visible-tokens]
               (assoc acc chain (into #{} (map keyword visible-tokens))))
             {}
             visible-tokens))

(defn rpc->pinned-mailservers [pinned-mailservers]
  (reduce-kv (fn [acc chain pinned-mailserver]
               (assoc acc chain (keyword pinned-mailserver)))
             {}
             pinned-mailservers))

(defn rpc->custom-bootnodes [custom-bootnodes]
  (reduce-kv (fn [acc chain custom-bootnodes]
               (assoc acc (str chain) custom-bootnodes))
             {}
             custom-bootnodes))

(defn rpc->settings [settings]
  (-> settings
      (update :dapps-address eip55/address->checksum)
      (update :address eip55/address->checksum)
      (update :networks/networks rpc->networks)
      (update :wallet/visible-tokens rpc->visible-tokens)
      (update :pinned-mailservers rpc->pinned-mailservers)
      (update :currency keyword)))
