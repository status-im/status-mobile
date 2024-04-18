(ns status-im.contexts.wallet.collectible.utils)

(defn collectible-balance
  [collectible]
  (-> collectible
      :ownership
      first
      :balance
      js/parseInt))
