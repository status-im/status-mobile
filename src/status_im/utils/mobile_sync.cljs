(ns status-im.utils.mobile-sync)

(defn cellular? [network-type]
  (= network-type "cellular"))

(defn syncing-allowed? [{:keys [db]}]
  (let [network (:network/type db)
        {:keys [syncing-on-mobile-network?]} (:multiaccount db)]
    (not (and (= network "cellular")
              (not syncing-on-mobile-network?)))))