(ns legacy.status-im.multiaccounts.model)

(defn logged-in?
  [{:keys [profile/profile]}]
  (boolean profile))
