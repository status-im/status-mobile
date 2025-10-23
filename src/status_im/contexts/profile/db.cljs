(ns status-im.contexts.profile.db)

(defn testnet?
  [db]
  (boolean (get-in db [:profile/profile :test-networks-enabled?])))
