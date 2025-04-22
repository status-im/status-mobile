(ns status-im.contexts.wallet.networks.filter)

(defn by-id
  [networks network-filter]
  (if (:by-id network-filter)
    (remove
     (comp (partial contains? (:by-id network-filter)) :chain-id)
     networks)
    networks))

(defn- toggle-by-id
  [filtered-chain-ids chain-id]
  (let [already-filtered? (contains? filtered-chain-ids chain-id)]
    (cond
      (nil? filtered-chain-ids) #{chain-id}
      already-filtered?         (disj filtered-chain-ids chain-id)
      (not already-filtered?)   (conj filtered-chain-ids chain-id)
      :else                     filtered-chain-ids)))

(defn toggle
  [filter-state filter-data]
  (condp #(contains? %2 %1) filter-data
    :by-id (update filter-state :by-id toggle-by-id (:by-id filter-data))
    filter-state))

(defn has-filters?
  [network-filter]
  (boolean
   (and (seq network-filter)
        (every? (complement empty?) (vals network-filter)))))
