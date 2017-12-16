(ns status-im.ui.screens.wallet.transactions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.money :as money]
            [status-im.utils.transactions :as transactions]
            [status-im.utils.hex :as utils.hex]))

(reg-sub :wallet.transactions/transactions-loading?
  :<- [:wallet]
  (fn [wallet]
    (:transactions-loading? wallet)))

(reg-sub :wallet.transactions/error-message?
  :<- [:wallet]
  (fn [wallet]
    (get-in wallet [:errors :transactions-update])))

(reg-sub :wallet.transactions/current-tab
  :<- [:wallet]
  (fn [wallet]
    (get wallet :current-tab 0)))

(defn enrich-transaction [{:keys [type to from timestamp] :as transaction} contacts]
  ;; TODO (yenda) proper wallet logic when wallet switching is implemented
  (let [[contact-address key-contact key-wallet] (if (= type :inbound)
                                                   [from :from-contact :to-wallet]
                                                   [to :to-contact :from-wallet])
        wallet                                   (i18n/label :main-wallet)
        contact                                  (get contacts (utils.hex/normalize-hex contact-address))]
    (cond-> transaction
      contact (assoc key-contact (:name contact))
      :always (assoc key-wallet wallet
                     :time-formatted (datetime/timestamp->time timestamp)))))

(reg-sub :wallet.transactions/transactions
  :<- [:wallet]
  :<- [:contacts/by-address]
  (fn [[wallet contacts]]
    (reduce (fn [acc [hash transaction]]
              (assoc acc hash (enrich-transaction transaction contacts)))
            {}
            (:transactions wallet))))

(reg-sub :wallet.transactions/grouped-transactions
  :<- [:wallet.transactions/transactions]
  (fn [transactions]
    (group-by :type (vals transactions))))

(defn- format-unsigned-transaction [{:keys [id] :as transaction}]
  (assoc transaction
         :type           :unsigned
         :confirmations  0
         ;; TODO (andrey) revisit this, we shouldn't set not hash value to the hash field
         :hash           id))

(reg-sub :wallet/unsigned-transactions
  :<- [:wallet]
  :<- [:contacts/by-address]
  (fn [[wallet contacts]]
    (map #(enrich-transaction % contacts) (vals (:transactions-unsigned wallet)))))

(reg-sub :wallet.transactions/unsigned-transactions
  :<- [:wallet/unsigned-transactions]
  (fn [transactions]
    (reduce (fn [acc {:keys [id] :as transaction}]
              (assoc acc id (format-unsigned-transaction transaction)))
            {}
            transactions)))

(reg-sub :wallet.transactions/unsigned-transactions-count
  :<- [:wallet.transactions/unsigned-transactions]
  (fn [unsigned-transactions]
    (count unsigned-transactions)))

(reg-sub :wallet.transactions/unsigned-transactions-list
  :<- [:wallet.transactions/unsigned-transactions]
  (fn [unsigned-transactions]
    (vals unsigned-transactions)))

(reg-sub :wallet.transactions/postponed-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [postponed]}]
    (when postponed
      {:title "Postponed"
       :key :postponed
       :data postponed})))

(reg-sub :wallet.transactions/pending-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [pending]}]
    (when pending
      {:title "Pending"
       :key :pending
       :data pending})))

(defn group-transactions-by-date [transactions]
  (->> transactions
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key)
       reverse
       (map (fn [[date-key transactions]]
              {:title (datetime/timestamp->mini-date (:timestamp (first transactions)))
               :key   date-key
               :data  (sort-by :timestamp > transactions)}))))

(reg-sub :wallet.transactions/completed-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [inbound outbound]}]
    (group-transactions-by-date (into inbound outbound))))

(reg-sub :wallet.transactions/transactions-history-list
  :<- [:wallet.transactions/postponed-transactions-list]
  :<- [:wallet.transactions/pending-transactions-list]
  :<- [:wallet.transactions/completed-transactions-list]
  (fn [[postponed pending completed]]
    (cond-> []
      postponed (into postponed)
      pending   (into pending)
      completed (into completed))))

(reg-sub :wallet.transactions/current-transaction
  :<- [:wallet]
  (fn [wallet]
    (:current-transaction wallet)))

(reg-sub :wallet.transactions/transaction-details
  :<- [:wallet.transactions/unsigned-transactions]
  :<- [:wallet.transactions/transactions]
  :<- [:wallet.transactions/current-transaction]
  :<- [:network]
  (fn [[unsigned-transactions transactions current-transaction network]]
    (let [transactions (merge transactions unsigned-transactions)
          {:keys [gas-used gas-price hash timestamp type] :as transaction} (get transactions current-transaction)]
      (when transaction
        (merge transaction
               {:gas-price-eth  (money/wei->str :eth gas-price)
                :gas-price-gwei (money/wei->str :gwei gas-price)
                :date           (datetime/timestamp->long-date timestamp)}
               (if (= type :unsigned)
                 {:block     (i18n/label :not-applicable)
                  :cost      (i18n/label :not-applicable)
                  :gas-limit (i18n/label :not-applicable)
                  :gas-used  (i18n/label :not-applicable)
                  :nonce     (i18n/label :not-applicable)
                  :hash      (i18n/label :not-applicable)}
                 {:cost (money/wei->str :eth (money/fee-value gas-used gas-price))
                  :url  (transactions/get-transaction-details-url network hash)}))))))

(reg-sub :wallet.transactions.details/confirmations
  :<- [:wallet.transactions/transaction-details]
  (fn [transaction-details]
    ;;TODO (yenda) this field should be calculated based on the current-block and the block of the transaction
    (:confirmations transaction-details)))

(reg-sub :wallet.transactions.details/confirmations-progress
  :<- [:wallet.transactions.details/confirmations]
  (fn [confirmations]
    (let [max-confirmations 10]
      (if (>= confirmations max-confirmations)
        100
        (* 100 (/ confirmations max-confirmations))))))

(reg-sub :wallet.transactions/filters
  (fn [db]
    (get-in db [:wallet.transactions :filters])))