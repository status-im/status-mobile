(ns status-im.contexts.wallet.networks.filter)

(defn by-id
  [networks network-filter]
  (if (:by-id network-filter)
    (filter
     (comp (partial contains? (:by-id network-filter)) :chain-id)
     networks)
    networks))
