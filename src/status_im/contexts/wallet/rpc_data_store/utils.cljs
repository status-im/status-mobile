(ns status-im.contexts.wallet.rpc-data-store.utils)

(defn extract-and-rename
  "Extract keys from a map and rename them"
  [m key-map]
  (reduce (fn [acc [old-key new-key]]
            (if (contains? m old-key)
              (assoc acc new-key (get m old-key))
              acc))
          {}
          key-map))
