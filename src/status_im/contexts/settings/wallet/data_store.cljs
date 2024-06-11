(ns status-im.contexts.settings.wallet.data-store)

(defn extract-keypair-name
  [db key-uids-set]
  (when (= (count key-uids-set) 1)
    (let [key-uid  (first key-uids-set)
          keypairs (get-in db [:wallet :keypairs])]
      (->> (filter #(= (:key-uid %) key-uid) keypairs)
           first
           :name))))

(defn update-keypair
  [keypairs key-uid update-fn]
  (mapcat (fn [keypair]
            (if (= (keypair :key-uid) key-uid)
              (if-let [updated (update-fn keypair)]
                [updated]
                [])
              [keypair]))
   keypairs))

(defn make-accounts-fully-operable
  [accounts key-uids-set]
  (reduce-kv
   (fn [acc k account]
     (if (and (contains? key-uids-set (:key-uid account))
              (= (keyword (:operable account)) :no))
       (assoc acc k (assoc account :operable :fully))
       (assoc acc k account)))
   {}
   accounts))

(defn- make-keypairs-accounts-fully-operable
  [accounts]
  (map (fn [account]
         (assoc account :operable :fully))
       accounts))

(defn make-keypairs-fully-operable
  [keypairs key-uids-set]
  (map (fn [keypair]
         (if (contains? key-uids-set (:key-uid keypair))
           (-> keypair
               (update :accounts make-keypairs-accounts-fully-operable)
               (assoc :lowest-operability :fully))
           keypair))
       keypairs))
