(ns status-im.contexts.settings.wallet.data-store)

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
   (fn [acc k {:keys [key-uid] :as account}]
     (if (and (contains? key-uids-set key-uid)
              (or (nil? operable-condition)
                  (= (:operable account) operable-condition)))
       (assoc acc k (assoc account :operable :fully))
       (assoc acc k account)))
   {}
   accounts))

(defn- make-keypairs-accounts-fully-operable
  [accounts operable-condition]
  (map (fn [account]
         (if (or (nil? operable-condition) (= (:operable account) operable-condition))
           (assoc account :operable :fully)
           account))
       accounts))

(defn make-keypairs-fully-operable
  "Updates keypairs' accounts to be fully operable based on specified key-uids and an optional operable condition.

  Parameters:
  - :keypairs (map): A map of keypair key-uid to keypair details.
  - :key-uids-set (set): A set of key-uids that need to be checked and updated.
  - :operable-condition (keyword, optional): The condition that the keypair's accounts' operability must meet to be updated.
    If nil or not provided, the function will update all keypairs' accounts.

  Returns:
  - A new map with keypairs updated to be fully operable where the conditions are met."
  [{:keys [keypairs key-uids-set operable-condition]}]
  (reduce-kv (fn [acc k keypair]
               (if (contains? key-uids-set k)
                 (assoc acc
                        k
                        (-> keypair
                            (update :accounts make-keypairs-accounts-fully-operable operable-condition)
                            (assoc :lowest-operability :fully)))
                 (assoc acc k keypair)))
             {}
             keypairs))

(defn get-keypair-key-uids-set-from-addresses
  [db addresses]
  (reduce (fn [key-uid-set address]
            (if-let [account-key-uid (get-in db [:wallet :accounts address :key-uid])]
              (conj key-uid-set account-key-uid)
              key-uid-set))
          #{}
          addresses))
