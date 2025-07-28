(ns status-im.contexts.wallet.account.db)

(defn get-accounts
  [db]
  (get-in db [:wallet :accounts]))

(defn get-accounts-addresses
  [db]
  (-> db get-accounts keys vec))
