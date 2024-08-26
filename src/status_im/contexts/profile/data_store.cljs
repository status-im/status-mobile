(ns status-im.contexts.profile.data-store)

(defn accepted-terms?
  [accounts]
  (some #(:hasAcceptedTerms %) accounts))
