(ns legacy.status-im.wallet.db
  (:require
    [legacy.status-im.utils.priority-map :refer [empty-transaction-map]]
    [utils.i18n :as i18n]
    [utils.money :as money]))

(defn- too-precise-amount?
  "Checks if number has any extra digit beyond the allowed number of decimals.
  It does so by checking the number against its rounded value."
  [amount decimals]
  (let [^js bn (money/bignumber amount)]
    (not (.eq bn
              (.round bn decimals)))))

(defn parse-amount
  [amount decimals]
  (when-not (empty? amount)
    (let [normalized-amount (money/normalize amount)
          value             (money/bignumber normalized-amount)]
      (cond
        (not (money/valid? value))
        {:error (i18n/label :t/validation-amount-invalid-number) :value value}

        (too-precise-amount? normalized-amount decimals)
        {:error (i18n/label :t/validation-amount-is-too-precise {:decimals decimals}) :value value}

        :else
        {:value value}))))

(def default-wallet-filters
  #{:inbound :outbound :pending :failed})

(def default-wallet
  {:filters default-wallet-filters})

(defn get-confirmations
  [{:keys [block]} current-block]
  (if (and current-block block)
    (inc (- current-block block))
    0))

(defn remove-transactions-since-block
  [accounts block]
  (reduce-kv (fn [acc account-address account]
               (assoc acc
                      account-address
                      (update account
                              :transactions
                              (fn [transactions]
                                (into empty-transaction-map
                                      (drop-while (fn [[_ v]]
                                                    (>= (int (:block v)) block))
                                                  transactions))))))
             {}
             accounts))
