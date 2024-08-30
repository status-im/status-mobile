(ns legacy.status-im.data-store.settings
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [legacy.status-im.data-store.visibility-status-updates :as visibility-status-updates]
    [status-im.contexts.wallet.common.validation :as validation]
    [utils.ethereum.eip.eip55 :as eip55]))

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

(defn rpc->currency
  [currency]
  (-> currency
      (name)
      (string/lower-case)
      (keyword)))

(defn rpc->settings
  [settings]
  (-> settings
      (update :dapps-address eip55/address->checksum)
      (update :address eip55/address->checksum)
      (update :wallet-legacy/visible-tokens rpc->visible-tokens)
      (update :pinned-mailservers rpc->pinned-mailservers)
      (update :link-previews-enabled-sites set)
      (update :currency rpc->currency)
      (assoc :ens-name? (validation/ens-name? (:preferred-name settings)))
      (visibility-status-updates/<-rpc-settings)
      (set/rename-keys {:compressedKey :compressed-key
                        :emojiHash     :emoji-hash})))

(defn rpc->setting-value
  [{:keys [name] :as setting}]
  (condp = name
    :currency (update setting :value rpc->currency)
    setting))
