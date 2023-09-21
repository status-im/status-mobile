(ns status-im2.subs.wallet.transactions
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.transactions.core :as transactions]
            [utils.i18n :as i18n]
            [status-im.notifications.core :as notifications]
            [utils.datetime :as datetime]
            [utils.money :as money]
            [status-im.wallet.db :as wallet.db]
            [status-im.wallet.utils :as wallet.utils]))

(re-frame/reg-sub
 :wallet/accounts
 :<- [:wallet]
 (fn [wallet]
   (get wallet :accounts)))

(re-frame/reg-sub
 :wallet/account-by-transaction-hash
 :<- [:wallet/accounts]
 (fn [accounts [_ tx-hash]]
   (some (fn [[address account]]
           (when-let [transaction (get-in account [:transactions tx-hash])]
             (assoc transaction :address address)))
         accounts)))

(re-frame/reg-sub
 :wallet/transactions
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:accounts address :transactions])))

(re-frame/reg-sub
 :wallet/filters
 :<- [:wallet]
 (fn [wallet]
   (get wallet :filters)))

(defn enrich-transaction
  [{:keys [type to from value token] :as transaction}
   contacts native-currency]
  (let [[contact-address key-contact key-wallet]
        (if (= type :inbound)
          [from :from-contact :to-wallet]
          [to :to-contact :from-wallet])
        wallet (i18n/label :main-wallet)
        contact (get contacts contact-address)
        {:keys [symbol-display decimals] :as asset}
        (or token native-currency)
        amount-text (if value
                      (wallet.utils/format-amount value decimals)
                      "...")
        currency-text (when asset
                        (clojure.core/name (or symbol-display
                                               (:symbol asset))))]
    (cond-> transaction
      contact (assoc key-contact (:name contact))
      :always (assoc key-wallet
                     wallet
                     :amount-text   amount-text
                     :currency-text currency-text))))

(re-frame/reg-sub
 :wallet.transactions/transactions
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet/transactions address])
    (re-frame/subscribe [:contacts/contacts-by-address])
    (re-frame/subscribe [:ethereum/native-currency])])
 (fn [[transactions contacts native-currency]]
   (reduce (fn [acc [tx-hash transaction]]
             (assoc acc
                    tx-hash
                    (enrich-transaction transaction contacts native-currency))) ;;TODO this doesn't
           ;;look good for
           ;;performance, we
           ;;need to calculate
           ;;this only once for
           ;;each transaction
           {}
           transactions)))

(re-frame/reg-sub
 :wallet.transactions/all-filters?
 :<- [:wallet/filters]
 (fn [filters]
   (= wallet.db/default-wallet-filters
      filters)))

(def filters-labels
  {:inbound  (i18n/label :t/incoming)
   :outbound (i18n/label :t/outgoing)
   :pending  (i18n/label :t/pending)
   :failed   (i18n/label :t/failed)})

(re-frame/reg-sub
 :wallet.transactions/filters
 :<- [:wallet/filters]
 (fn [filters]
   (map (fn [id]
          (let [checked? (filters id)]
            {:id       id
             :label    (filters-labels id)
             :checked? checked?
             :on-touch #(if checked?
                          (re-frame/dispatch [:wallet.transactions/remove-filter id])
                          (re-frame/dispatch [:wallet.transactions/add-filter id]))}))
        wallet.db/default-wallet-filters)))

(re-frame/reg-sub
 :wallet.transactions.filters/screen
 :<- [:wallet.transactions/filters]
 :<- [:wallet.transactions/all-filters?]
 (fn [[filters all-filters?]]
   {:all-filters?        all-filters?
    :filters             filters
    :on-touch-select-all (when-not all-filters?
                           #(re-frame/dispatch
                             [:wallet.transactions/add-all-filters]))}))

(defn- enrich-transaction-for-list
  [filters
   {:keys [type from-contact from to-contact to timestamp] :as transaction}
   address]
  (when (filters type)
    (assoc
     (case type
       :inbound
       (assoc transaction
              :label                       (i18n/label :t/from-capitalized)
              :contact-accessibility-label :sender-text
              :address-accessibility-label :sender-address-text
              :contact                     from-contact
              :address                     from)
       (assoc transaction
              :label                       (i18n/label :t/to-capitalized)
              :contact-accessibility-label :recipient-name-text
              :address-accessibility-label :recipient-address-text
              :contact                     to-contact
              :address                     to))
     :time-formatted (datetime/timestamp->time timestamp)
     :on-touch-fn    #(re-frame/dispatch [:wallet.ui/show-transaction-details
                                          (:hash transaction)
                                          address]))))

(defn group-transactions-by-date
  [transactions]
  (->> transactions
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key >)
       (map (fn [[date-key transactions]]
              {:title (datetime/timestamp->mini-date (:timestamp (first transactions)))
               :key   date-key
               :data  (sort-by :timestamp > transactions)}))))

(re-frame/reg-sub
 :wallet.transactions.history/screen
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet.transactions/transactions address])
    (re-frame/subscribe [:wallet/filters])
    (re-frame/subscribe [:wallet.transactions/all-filters?])])
 (fn [[transactions filters all-filters?] [_ address]]
   {:all-filters? all-filters?
    :total (count transactions)
    :transaction-history-sections
    (->> transactions
         vals
         (keep #(enrich-transaction-for-list filters % address))
         (group-transactions-by-date))}))

(re-frame/reg-sub
 :wallet/recipient-recent-txs
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet.transactions/transactions address])])
 (fn [[transactions] _]
   (->> transactions
        vals
        (sort-by :timestamp >)
        (remove #(= (:type %) :pending))
        (take 3))))

(re-frame/reg-sub
 :wallet.transactions.details/current-transaction
 (fn [[_ _ address] _]
   [(re-frame/subscribe [:wallet.transactions/transactions address])
    (re-frame/subscribe [:ethereum/native-currency])
    (re-frame/subscribe [:chain-id])])
 (fn [[transactions native-currency chain-id] [_ tx-hash _]]
   (let [{:keys [gas-used gas-price fee-cap tip-cap timestamp type]
          :as   transaction}
         (get transactions tx-hash)
         native-currency-text (name (or (:symbol-display native-currency)
                                        (:symbol native-currency)))]
     (when transaction
       (merge transaction
              {:gas-price-eth  (if gas-price
                                 (money/wei->str :eth
                                                 gas-price
                                                 native-currency-text)
                                 "-")
               :gas-price-gwei (if gas-price
                                 (money/wei->str :gwei
                                                 gas-price)
                                 "-")
               :fee-cap-gwei   (if fee-cap
                                 (money/wei->str :gwei
                                                 fee-cap)
                                 "-")
               :tip-cap-gwei   (if tip-cap
                                 (money/wei->str :gwei
                                                 tip-cap)
                                 "-")
               :date           (datetime/timestamp->long-date timestamp)}
              (if (= type :unsigned)
                {:block     (i18n/label :not-applicable)
                 :cost      (i18n/label :not-applicable)
                 :gas-limit (i18n/label :not-applicable)
                 :gas-used  (i18n/label :not-applicable)
                 :nonce     (i18n/label :not-applicable)
                 :hash      (i18n/label :not-applicable)}
                {:cost (when gas-used
                         (money/wei->str :eth
                                         (money/fee-value gas-used gas-price)
                                         native-currency-text))
                 :url  (transactions/get-transaction-details-url
                        chain-id
                        (:hash transaction))}))))))

(re-frame/reg-sub
 :wallet.transactions.details/screen
 (fn [[_ tx-hash address] _]
   [(re-frame/subscribe [:wallet.transactions.details/current-transaction tx-hash address])
    (re-frame/subscribe [:ethereum/current-block])])
 (fn [[transaction current-block]]
   (let [confirmations (wallet.db/get-confirmations transaction
                                                    current-block)]
     (assoc transaction
            :confirmations confirmations
            :confirmations-progress
            (if (>= confirmations transactions/confirmations-count-threshold)
              100
              (* 100 (/ confirmations transactions/confirmations-count-threshold)))))))

(re-frame/reg-sub
 :notifications/wallet-transactions
 :<- [:push-notifications/preferences]
 (fn [pref]
   (first (filter #(notifications/preference= %
                                              {:service    "wallet"
                                               :event      "transaction"
                                               :identifier "all"})
                  pref))))
