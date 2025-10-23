(ns status-im.contexts.network.db)

(defn online?
  [{:network/keys [status]}]
  (= :online status))
