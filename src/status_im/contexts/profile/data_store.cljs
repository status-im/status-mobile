(ns status-im.contexts.profile.data-store)

(defn accepted-terms?
  [accounts]
  (some :hasAcceptedTerms accounts))

(defn testnet?
  [db]
  (get-in db [:profile/profile :test-networks-enabled?]))
