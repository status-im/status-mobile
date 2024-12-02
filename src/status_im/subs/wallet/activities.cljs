(ns status-im.subs.wallet.activities
  (:require
    [quo.foundations.resources :as resources]
    [re-frame.core :as rf]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.activity-tab.constants :as activity-constants]
    [utils.collection :as collection]
    [utils.datetime :as datetime]
    [utils.hex :as hex]
    [utils.money :as money]))

(def ^:private precision 6)

(defn- hex->number
  [hex-str]
  (-> hex-str
      hex/normalize-hex
      money/from-hex
      str))

(defn- hex->amount
  [hex-str]
  (-> hex-str
      hex->number
      money/wei->ether
      (money/with-precision precision)
      str))

(defn- get-token-amount
  [{:keys [token-type]} amount]
  (if (#{activity-constants/wallet-activity-token-type-erc-721
         activity-constants/wallet-activity-token-type-erc-1155}
       token-type)
    (hex->number amount)
    (hex->amount amount)))

(defn- get-address-context-tag
  [accounts-and-saved-addresses address]
  (let [{:keys [context-type name emoji color customization-color]}
        (get accounts-and-saved-addresses address)]
    (case context-type
      :account
      {:type                :account
       :account-name        name
       :emoji               emoji
       :customization-color color}

      :saved-address
      {:type                :default
       :full-name           name
       :customization-color customization-color}

      {:type :address :address address})))

(defn- get-spender-context-tag
  [address]
  (let [swap-providers                       (->> constants/swap-providers
                                                  vals
                                                  (collection/index-by :contract-address))
        {:keys [full-name name] :as spender} (get swap-providers address)]
    (if spender
      {:type         :network
       :network-name full-name
       :network-logo (resources/get-network name)}
      {:type :address :address address})))

(defn- process-base-activity
  [{:keys [timestamp sender recipient token-in token-out chain-id-in chain-id-out activity-status]
    :as   activity}
   {:keys [chain-id->network-details accounts-and-saved-addresses]}]
  (let [token-id    (some-> (or token-in token-out)
                            :token-id
                            hex->number)
        network-in  (chain-id->network-details chain-id-in)
        network-out (chain-id->network-details chain-id-out)]
    (assoc activity
           :relative-date    (datetime/timestamp->relative (* timestamp 1000))
           :sender-tag       (get-address-context-tag accounts-and-saved-addresses sender)
           :recipient-tag    (get-address-context-tag accounts-and-saved-addresses recipient)
           :network-name-in  (:full-name network-in)
           :network-logo-in  (resources/get-network (:network-name network-in))
           :network-name-out (:full-name network-out)
           :network-logo-out (resources/get-network (:network-name network-out))
           :status           (activity-constants/wallet-activity-status->name activity-status)
           :token-id         token-id)))

(defn- process-activity
  [{:keys [activity-type token-out amount-out token-in amount-in approval-spender] :as activity} context]
  (let [base-activity (process-base-activity activity context)]
    (condp = activity-type
      activity-constants/wallet-activity-type-send
      (assoc base-activity
             :tx-type    :send
             :amount-out (get-token-amount token-out amount-out))

      activity-constants/wallet-activity-type-bridge
      (assoc base-activity
             :tx-type    :bridge
             :amount-out (get-token-amount token-out amount-out))

      activity-constants/wallet-activity-type-swap
      (assoc base-activity
             :tx-type    :swap
             :amount-in  (get-token-amount token-in amount-in)
             :amount-out (get-token-amount token-out amount-out))

      activity-constants/wallet-activity-type-approval
      (assoc base-activity
             :tx-type     :approval
             :amount-out  (get-token-amount token-out amount-out)
             :spender-tag (get-spender-context-tag approval-spender))

      nil)))

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(rf/reg-sub
 :wallet/activity-tab
 :<- [:wallet/ui]
 :-> :activity-tab)

(rf/reg-sub
 :wallet/activity-tab-request
 :<- [:wallet/activity-tab]
 :-> :request)

(rf/reg-sub
 :wallet/activity-tab-loading?
 :<- [:wallet/activity-tab-request]
 :-> :loading?)

(rf/reg-sub
 :wallet/accounts-and-saved-addresses
 :<- [:wallet/accounts-without-assets]
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [[accounts saved-addresses]]
   (merge
    (collection/index-by :address (map #(assoc % :context-type :account) accounts))
    (collection/index-by :address
                         (map #(assoc % :context-type :saved-address) (vals saved-addresses))))))

(rf/reg-sub
 :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/network-details]
 :<- [:wallet/accounts-and-saved-addresses]
 (fn [[activities current-address network-details accounts-and-saved-addresses]]
   (let [context    {:chain-id->network-details    (collection/index-by :chain-id network-details)
                     :accounts-and-saved-addresses accounts-and-saved-addresses}
         activities (->> (get activities current-address)
                         vals
                         (keep #(process-activity % context))
                         (sort-by :timestamp >)
                         (group-by #(datetime/timestamp->relative-short-date
                                     (* (:timestamp %) 1000))))]
     (->> activities
          (map (fn [[date activities]]
                 {:title     date
                  :data      activities
                  :timestamp (:timestamp (first activities))}))
          (sort-by :timestamp >)))))
