(ns status-im.contexts.wallet.tokens.db)

(defn get-tokens-map
  [db]
  (get-in db [:wallet :tokens]))

(defn get-token-symbols
  [db]
  (-> db get-tokens-map :symbols))
