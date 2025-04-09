(ns status-im.contexts.profile.data-store)

(defn accepted-terms?
  [accounts]
  (some :hasAcceptedTerms accounts))

(defn recently-opened-profile
  [profiles]
  (first (sort-by :timestamp > (vals profiles))))
