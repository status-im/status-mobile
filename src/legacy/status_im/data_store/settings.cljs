(ns legacy.status-im.data-store.settings
  (:require
    [clojure.set :as set]
    [legacy.status-im.data-store.visibility-status-updates :as visibility-status-updates]
    [status-im2.config :as config]
    [utils.ethereum.eip.eip55 :as eip55]))

(defn rpc->networks
  [networks]
  (reduce (fn [acc {:keys [id] :as network}]
            (assoc acc id network))
          {}
          (if (seq networks)
            networks
            config/default-networks)))

(defn rpc->visible-tokens
  [visible-tokens]
  (reduce-kv (fn [acc chain visible-tokens]
               (assoc acc chain (into #{} (map keyword visible-tokens))))
             {}
             visible-tokens))

(defn rpc->pinned-mailservers
  [pinned-mailservers]
  (reduce-kv (fn [acc chain pinned-mailserver]
               (assoc acc chain (keyword pinned-mailserver)))
             {}
             pinned-mailservers))

(defn rpc->custom-bootnodes
  [custom-bootnodes]
  (reduce-kv (fn [acc chain custom-bootnodes]
               (assoc acc (name chain) custom-bootnodes))
             {}
             custom-bootnodes))

(defn rpc->settings
  [settings]
  (-> settings
      (update :dapps-address eip55/address->checksum)
      (update :address eip55/address->checksum)
      (update :networks/networks rpc->networks)
      (update :networks/current-network #(if (seq %) % config/default-network))
      (update :wallet-legacy/visible-tokens rpc->visible-tokens)
      (update :pinned-mailservers rpc->pinned-mailservers)
      (update :link-previews-enabled-sites set)
      (update :custom-bootnodes rpc->custom-bootnodes)
      (update :custom-bootnodes-enabled? rpc->custom-bootnodes)
      (update :currency keyword)
      (visibility-status-updates/<-rpc-settings)
      (set/rename-keys {:compressedKey :compressed-key
                        :emojiHash     :emoji-hash})))
