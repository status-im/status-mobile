(ns status-im.contexts.network.data-store)

(defn online?
  [{:network/keys [status]}]
  (= :online status))
