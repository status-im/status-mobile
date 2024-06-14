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
  "Updates accounts to be fully operable based on specified key-uids and an optional operable condition.

  Parameters:
  - :accounts (map): A map of account addresses to account details.
  - :key-uids-set (set): A set of key-uids that need to be checked and updated.
  - :operable-condition (keyword, optional): The condition that the account's operability must meet to be updated.
    If nil or not provided, the function will update all accounts.

  Returns:
  - A new map with accounts updated to be fully operable where the conditions are met."
  [{:keys [accounts key-uids-set operable-condition]}]
  (reduce-kv
   (fn [acc k account]
     (if (and (contains? key-uids-set (:key-uid account))
              (or (nil? operable-condition)
                  (= (keyword (:operable account)) operable-condition)))
       (assoc acc k (assoc account :operable :fully))
       (assoc acc k account)))
   {}
   accounts))

(defn- make-keypairs-accounts-fully-operable
  [accounts operable-condition]
  (map (fn [account]
         (if (or (nil? operable-condition) (= (keyword (:operable account)) operable-condition))
           (assoc account :operable :fully)
           account))
       accounts))

(defn make-keypairs-fully-operable
  "Updates keypairs' accounts to be fully operable based on specified key-uids and an optional operable condition.

  Parameters:
  - :keypairs (seq): A sequence of keypair maps.
  - :key-uids-set (set): A set of key-uids that need to be checked and updated.
  - :operable-condition (keyword, optional): The condition that the keypair's accounts' operability must meet to be updated.
    If nil or not provided, the function will update all keypairs' accounts.

  Returns:
  - A new sequence with keypairs updated to be fully operable where the conditions are met."
  [{:keys [keypairs key-uids-set operable-condition]}]
  (map (fn [keypair]
         (if (contains? key-uids-set (:key-uid keypair))
           (-> keypair
               (update :accounts #(make-keypairs-accounts-fully-operable % operable-condition))
               (assoc :lowest-operability :fully))
           keypair))
       keypairs))

(defn map-addresses-to-key-uids
  [db addresses]
  (reduce (fn [key-uid-set address]
            (if-let [account (get-in db [:wallet :accounts address])]
              (conj key-uid-set (:key-uid account))
              key-uid-set))
          #{}
          addresses))
