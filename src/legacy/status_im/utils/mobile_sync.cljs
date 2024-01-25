(ns legacy.status-im.utils.mobile-sync)

(defn cellular?
  [network-type]
  (= network-type "cellular"))

(defn syncing-allowed?
  [{:keys [db]}]
  (let [network                              (:network/type db)
        {:keys [syncing-on-mobile-network?]} (:profile/profile db)]
    (or (= network "wifi")
        (and syncing-on-mobile-network?
             (= network "cellular")))))
