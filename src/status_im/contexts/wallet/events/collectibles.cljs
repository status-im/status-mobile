(ns status-im.contexts.wallet.events.collectibles
  (:require [camel-snake-kebab.extras :as cske]
            [taoensso.timbre :as log]
            [utils.ethereum.chain :as chain]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(def collectible-data-types
  {:unique-id        0
   :header           1
   :details          2
   :community-header 3})

(def fetch-type
  {:never-fetch         0
   :always-fetch        1
   :fetch-if-not-cached 2
   :fetch-if-cache-old  3})

(def max-cache-age-seconds 3600)
(def collectibles-request-batch-size 25)

(defn- move-collectibles-to-accounts
  [accounts new-collectibles-per-account]
  (reduce-kv (fn [acc account new-collectibles]
               (update-in acc [account :collectibles] #(reduce conj (or % []) new-collectibles)))
             accounts
             new-collectibles-per-account))

(defn flush-collectibles
  [{:keys [db]}]
  (let [collectibles-per-account (get-in db [:wallet :ui :collectibles :fetched])]
    {:db (-> db
             (update-in [:wallet :ui :collectibles] dissoc :pending-requests :fetched)
             (update-in [:wallet :accounts] move-collectibles-to-accounts collectibles-per-account))}))

(rf/reg-event-fx :wallet/flush-collectibles-fetched flush-collectibles)

(defn clear-stored-collectibles
  [{:keys [db]}]
  {:db (update-in db [:wallet :accounts] update-vals #(dissoc % :collectibles))})

(rf/reg-event-fx :wallet/clear-stored-collectibles clear-stored-collectibles)

(defn store-last-collectible-details
  [{:keys [db]} [collectible]]
  {:db       (assoc-in db [:wallet :last-collectible-details] collectible)
   :dispatch [:navigate-to :wallet-collectible]})

(rf/reg-event-fx :wallet/store-last-collectible-details store-last-collectible-details)

(rf/reg-event-fx
 :wallet/request-new-collectibles-for-account
 (fn [{:keys [db]} [{:keys [request-id account amount]}]]
   (let [current-collectible-idx (get-in db [:wallet :accounts account :current-collectible-idx] 0)
         collectibles-filter     nil
         data-type               (collectible-data-types :header)
         fetch-criteria          {:fetch-type            (fetch-type :fetch-if-not-cached)
                                  :max-cache-age-seconds max-cache-age-seconds}
         chain-ids               (chain/chain-ids db)
         request-params          [request-id
                                  chain-ids
                                  [account]
                                  collectibles-filter
                                  current-collectible-idx
                                  amount
                                  data-type
                                  fetch-criteria]]
     {:fx [[:json-rpc/call
            [{:method   "wallet_getOwnedCollectiblesAsync"
              :params   request-params
              :on-error (fn [error]
                          (log/error "failed to request collectibles for account"
                                     {:event  :wallet/request-new-collectibles-for-account
                                      :error  error
                                      :params request-params}))}]]]})))

(rf/reg-event-fx
 :wallet/request-collectibles-for-all-accounts
 (fn [{:keys [db]} [{:keys [new-request?]}]]
   (let [accounts                   (->> (get-in db [:wallet :accounts])
                                         (filter (fn [[_ {:keys [has-more-collectibles?]}]]
                                                   (or (nil? has-more-collectibles?)
                                                       (true? has-more-collectibles?))))
                                         (keys))
         num-accounts               (count accounts)
         collectibles-per-account   (quot collectibles-request-batch-size num-accounts)
         request-collectible-effect (fn [idx account]
                                      [:dispatch
                                       [:wallet/request-new-collectibles-for-account
                                        {:request-id idx
                                         :account    account
                                         :amount     collectibles-per-account}]])]
     {:db (cond-> db
            :always (assoc-in [:wallet :ui :collectibles :pending-requests] num-accounts)
            new-request? (update-in [:wallet :accounts] update-vals #(dissoc % :collectibles)))
      :fx (map-indexed request-collectible-effect accounts)})))

(rf/reg-event-fx
 :wallet/request-collectibles-for-current-viewing-account
 (fn [{:keys [db]} _]
   (let [current-viewing-account (-> db :wallet :current-viewing-account-address)]
     {:db (assoc-in db [:wallet :ui :collectibles :pending-requests] 1)
      :fx [[:dispatch [:wallet/request-new-collectibles-for-account
                       {:request-id 0
                        :account    current-viewing-account
                        :amount     collectibles-request-batch-size}]]]})))

(rf/reg-event-fx
 :wallet/owned-collectibles-filtering-done
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [{:keys [offset ownershipStatus collectibles
                 hasMore]} (transforms/json->clj message)
         collectibles     (cske/transform-keys transforms/->kebab-case-keyword collectibles)
         owner-address    (->> ownershipStatus first key name)
         pending-requests (dec (get-in db [:wallet :ui :collectibles :pending-requests]))
         collectible-idx  (+ offset (count collectibles))]
     {:db (-> db
              (assoc-in [:wallet :ui :collectibles :pending-requests] pending-requests)
              (assoc-in [:wallet :ui :collectibles :fetched owner-address] collectibles)
              (assoc-in [:wallet :accounts owner-address :current-collectible-idx] collectible-idx)
              (assoc-in [:wallet :accounts owner-address :has-more-collectibles?] hasMore))
      :fx [(when (zero? pending-requests)
             [:dispatch [:wallet/flush-collectibles-fetched]])]})))

(rf/reg-event-fx
 :wallet/get-collectible-details
 (fn [_ [collectible-id]]
   (let [request-id               0
         collectible-id-converted (cske/transform-keys transforms/->PascalCaseKeyword collectible-id)
         data-type                (collectible-data-types :details)
         request-params           [request-id [collectible-id-converted] data-type]]
     {:fx [[:json-rpc/call
            [{:method   "wallet_getCollectiblesByUniqueIDAsync"
              :params   request-params
              :on-error (fn [error]
                          (log/error "failed to request collectible"
                                     {:event  :wallet/get-collectible-details
                                      :error  error
                                      :params request-params}))}]]]})))

(rf/reg-event-fx
 :wallet/get-collectible-details-done
 (fn [_ [{:keys [message]}]]
   (let [response               (cske/transform-keys transforms/->kebab-case-keyword
                                                     (transforms/json->clj message))
         {:keys [collectibles]} response
         collectible            (first collectibles)]
     (if collectible
       {:fx [[:dispatch [:wallet/store-last-collectible-details collectible]]]}
       (log/error "failed to get collectible details"
                  {:event    :wallet/get-collectible-details-done
                   :response response})))))

(rf/reg-event-fx
 :wallet/clear-last-collectible-details
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet] dissoc :last-collectible-details)}))
