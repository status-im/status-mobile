(ns status-im.contexts.wallet.collectible.events
  (:require [camel-snake-kebab.extras :as cske]
            [clojure.set]
            [clojure.string :as string]
            [react-native.platform :as platform]
            [status-im.contexts.network.data-store :as network.data-store]
            [status-im.contexts.wallet.collectible.utils :as collectible-utils]
            [status-im.contexts.wallet.data-store :as data-store]
            [taoensso.timbre :as log]
            [utils.collection]
            [utils.ethereum.chain :as chain]
            [utils.number :as utils.number]
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

(def ownership-state
  {:idle     1
   :delayed  2
   :updating 3
   :error    4})

(defn- move-collectibles-to-accounts
  [accounts new-collectibles-per-account]
  (reduce-kv (fn [acc account new-collectibles]
               (update-in acc [account :collectibles] #(reduce conj (or % []) new-collectibles)))
             accounts
             new-collectibles-per-account))

(defn flush-collectibles
  [{:keys [db]}]
  (let [collectibles-per-account (get-in db [:wallet :ui :collectibles :fetched])
        updated-accounts         (move-collectibles-to-accounts (get-in db [:wallet :accounts])
                                                                collectibles-per-account)
        has-collectibles?        (some (fn [account]
                                         (pos? (count (:collectibles account))))
                                       (vals updated-accounts))]
    {:db (-> db
             (update-in [:wallet :ui :collectibles] dissoc :pending-requests :fetched)
             (assoc-in [:wallet :accounts] updated-accounts))
     :fx [[:dispatch
           [:centralized-metrics/track :metric/wallet-collectibles-fetched
            {:has-collectibles? has-collectibles?}]]]}))

(rf/reg-event-fx :wallet/flush-collectibles-fetched flush-collectibles)

(defn clear-stored-collectibles
  [{:keys [db]}]
  {:db (update-in db [:wallet :accounts] update-vals #(dissoc % :collectibles))})

(rf/reg-event-fx :wallet/clear-stored-collectibles clear-stored-collectibles)

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

(defonce collectibles-request-ids (atom 0))

(defn- get-unique-collectible-request-id
  [amount]
  (let [initial-id (deref collectibles-request-ids)
        last-id    (+ initial-id amount)]
    (reset! collectibles-request-ids last-id)
    (range initial-id last-id)))

(rf/reg-event-fx
 :wallet/request-collectibles-for-all-accounts
 (fn [{:keys [db]} [{:keys [new-request?]}]]
   (let [updating-addresses       (-> (get-in db [:wallet :ui :collectibles :updating])
                                      keys
                                      set)
         accounts                 (->> (get-in db [:wallet :accounts])
                                       (filter (fn [[_ {:keys [has-more-collectibles?]}]]
                                                 (or (nil? has-more-collectibles?)
                                                     (true? has-more-collectibles?))))
                                       (keys))
         ;; filter the addresses which are requested before and the collectibles are updating
         requestable-addresses    (clojure.set/difference (set accounts)
                                                          updating-addresses)
         num-accounts             (count requestable-addresses)
         collectibles-per-account (quot collectibles-request-batch-size num-accounts)
         ;; We need to pass unique IDs for simultaneous requests, otherwise they'll fail
         request-ids              (get-unique-collectible-request-id num-accounts)
         collectible-requests     (map (fn [id account]
                                         [:dispatch
                                          [:wallet/request-new-collectibles-for-account
                                           {:request-id id
                                            :account    account
                                            :amount     collectibles-per-account}]])
                                       request-ids
                                       requestable-addresses)]
     {:db (cond-> db
            :always      (assoc-in [:wallet :ui :collectibles :pending-requests] num-accounts)
            new-request? (update-in [:wallet :accounts] update-vals #(dissoc % :collectibles)))
      :fx collectible-requests})))

(defn request-collectibles-for-account
  [{:keys [db]} [address]]
  (let [pending-requests (get-in db [:wallet :ui :collectibles :pending-requests] 0)
        [request-id]     (get-unique-collectible-request-id 1)]
    {:db (assoc-in db [:wallet :ui :collectibles :pending-requests] (inc pending-requests))
     :fx [[:dispatch
           [:wallet/request-new-collectibles-for-account
            {:request-id request-id
             :account    address
             :amount     collectibles-request-batch-size}]]]}))

(rf/reg-event-fx :wallet/request-collectibles-for-account request-collectibles-for-account)

(rf/reg-event-fx
 :wallet/request-collectibles-for-current-viewing-account
 (fn [{:keys [db]} _]
   (when (network.data-store/online? db)
     (let [current-viewing-account (-> db :wallet :current-viewing-account-address)]
       {:fx [[:dispatch [:wallet/request-collectibles-for-account current-viewing-account]]]}))))

(rf/reg-event-fx
 :wallet/collectible-ownership-update-finished-with-error
 (fn [{:keys [db]} [{:keys [accounts message chainId]}]]
   (let [address            (first accounts)
         pending-chain-ids  (get-in db [:wallet :ui :collectibles :updating address])
         updated-chain-ids  (disj pending-chain-ids chainId)
         all-chain-updated? (and (some? pending-chain-ids) (empty? updated-chain-ids))]
     {:db (cond-> db
            (some? pending-chain-ids)
            (assoc-in [:wallet :ui :collectibles :updating address] updated-chain-ids))
      :fx [[:dispatch
            [:wallet/log-rpc-error
             {:event  :wallet/collectible-ownership-update-finished-with-error
              :params {:address  address
                       :chain-id chainId}}
             message]]
           (when all-chain-updated?
             [:dispatch [:wallet/request-collectibles-for-account address]])]})))

(rf/reg-event-fx
 :wallet/collectible-ownership-update-finished
 (fn [{:keys [db]} [{:keys [accounts chainId]}]]
   (let [address            (first accounts)
         pending-chain-ids  (get-in db [:wallet :ui :collectibles :updating address])
         updated-chain-ids  (disj pending-chain-ids chainId)
         all-chain-updated? (and (some? pending-chain-ids) (empty? updated-chain-ids))]
     {:db (cond-> db
            (some? pending-chain-ids)
            (assoc-in [:wallet :ui :collectibles :updating address] updated-chain-ids))
      :fx [(when all-chain-updated?
             [:dispatch [:wallet/request-collectibles-for-account address]])]})))

(defn- update-collectibles-in-account
  [existing-collectibles updated-collectibles]
  (let [indexed-existing (utils.collection/index-by
                          :unique-id
                          existing-collectibles)
        existing-ids     (-> indexed-existing keys vec)
        ;; pick collectibles only in the app-db
        indexed-updated  (-> (utils.collection/index-by
                              :unique-id
                              updated-collectibles)
                             (select-keys existing-ids))]
    (-> (merge-with
         merge
         indexed-existing
         indexed-updated)
        vals
        vec)))

(rf/reg-event-fx
 :wallet/collectibles-data-updated
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [collectibles-by-address (->> message
                                      transforms/json->clj
                                      data-store/rpc->collectibles
                                      (group-by #(-> % :ownership first :address)))]
     {:db (update-in db
                     [:wallet :accounts]
                     #(reduce-kv
                       (fn [accounts address updated-collectibles]
                         (if (contains? accounts address)
                           (update-in accounts
                                      [address :collectibles]
                                      update-collectibles-in-account
                                      updated-collectibles)
                           accounts))
                       %
                       collectibles-by-address))})))

(rf/reg-event-fx
 :wallet/set-collectibles-updating-status
 (fn [{:keys [db]} [address chain-ids]]
   {:db (assoc-in db [:wallet :ui :collectibles :updating address] chain-ids)}))

(defn- update-fetched-collectibles-progress
  [db owner-address collectibles offset has-more?]
  (-> db
      (update-in [:wallet :ui :collectibles :updating] dissoc owner-address)
      (assoc-in [:wallet :ui :collectibles :fetched owner-address] collectibles)
      (assoc-in [:wallet :accounts owner-address :current-collectible-idx]
                (+ offset (count collectibles)))
      (assoc-in [:wallet :accounts owner-address :has-more-collectibles?] has-more?)))

(defn- updating-collectibles?
  [status]
  (and (= (:state status) (ownership-state :updating))
       (= (:timestamp status) -1)))

(rf/reg-event-fx
 :wallet/owned-collectibles-filtering-done
 (fn [{:keys [db]} [{:keys [message]}]]
   (let [{:keys [offset ownershipStatus collectibles
                 hasMore]}           (transforms/json->clj message)
         ownership-status-by-address (-> ownershipStatus
                                         (update-keys name)
                                         (update-vals #(update-keys %
                                                                    (comp utils.number/parse-int name))))
         owner-address               (some-> ownership-status-by-address
                                             first
                                             key)
         ownership-status            (get ownership-status-by-address owner-address)
         collectibles                (->> collectibles
                                          data-store/rpc->collectibles
                                          (map #(update % :ownership distinct))
                                          vec)
         pending-requests            (dec (get-in db [:wallet :ui :collectibles :pending-requests]))
         ;; check if collectibles are updating (never fetched and cached before) for this address
         updating-chains             (-> (select-keys
                                          ownership-status
                                          (for [[k v] ownership-status
                                                :when (updating-collectibles? v)]
                                            k))
                                         keys
                                         set)
         updating?                   (-> updating-chains count pos?)]
     {:db (cond-> db
            :always       (assoc-in [:wallet :ui :collectibles :pending-requests] pending-requests)
            owner-address (update-fetched-collectibles-progress owner-address
                                                                collectibles
                                                                offset
                                                                (when-not updating? hasMore)))
      :fx [(when (zero? pending-requests)
             [:dispatch [:wallet/flush-collectibles-fetched]])
           (when updating?
             [:dispatch [:wallet/set-collectibles-updating-status owner-address updating-chains]])]})))

(rf/reg-event-fx
 :wallet/navigate-to-collectible-details
 (fn [{:keys [db]}
      [{{collectible-id :id :as collectible} :collectible
        aspect-ratio                         :aspect-ratio
        gradient-color                       :gradient-color}]]
   (let [request-id               0
         collectible-id-converted (cske/transform-keys transforms/->PascalCaseKeyword collectible-id)
         data-type                (collectible-data-types :details)
         request-params           [request-id [collectible-id-converted] data-type]]
     {:db (assoc-in db
           [:wallet :ui :collectible]
           {:details        collectible
            :aspect-ratio   aspect-ratio
            :gradient-color gradient-color})
      :fx [[:json-rpc/call
            [{:method   "wallet_getCollectiblesByUniqueIDAsync"
              :params   request-params
              :on-error (fn [error]
                          (log/error "failed to request collectible"
                                     {:event  :wallet/navigate-to-collectible-details
                                      :error  error
                                      :params request-params}))}]]
           ;; We delay the navigation because we need re-frame to update the DB on time.
           ;; By doing it, we skip a blink while visiting the collectible detail page.
           [:dispatch-later
            {:ms       17
             :dispatch [:navigate-to :screen/wallet.collectible]}]]})))

(defn- keep-not-empty-value
  [old-value new-value]
  (if (or (string/blank? new-value) (nil? new-value))
    old-value
    new-value))

(rf/reg-event-fx
 :wallet/get-collectible-details-done
 (fn [{db :db} [{:keys [message]}]]
   (let [response                      (cske/transform-keys transforms/->kebab-case-keyword
                                                            (transforms/json->clj message))
         {[collectible] :collectibles} response
         known-collectible-info        (get-in db [:wallet :ui :collectible :details])
         merged-collectible            (as-> known-collectible-info c
                                         (merge-with keep-not-empty-value
                                                     c
                                                     collectible)
                                         (update c :ownership distinct))]
     (if collectible
       {:db (assoc-in db [:wallet :ui :collectible :details] merged-collectible)}
       (log/error "failed to get collectible details"
                  {:event    :wallet/get-collectible-details-done
                   :response response})))))

(rf/reg-event-fx
 :wallet/clear-collectible-details
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :collectible)}))

(rf/reg-event-fx :wallet/trigger-share-collectible
 (fn [_ [{:keys [title uri]}]]
   {:fx [[:dispatch
          [:open-share
           {:options (if platform/ios?
                       {:activityItemSources
                        [{:placeholderItem {:type    :text
                                            :content uri}
                          :item            {:default {:type    :url
                                                      :content uri}}
                          :linkMetadata    {:title title}}]}
                       {:title     title
                        :subject   title
                        :url       uri
                        :isNewTask true})}]]]}))

(rf/reg-event-fx :wallet/share-collectible
 (fn [{:keys [db]} [{:keys [title token-id contract-address chain-id]}]]
   (let [uri (collectible-utils/get-opensea-collectible-url
              {:chain-id               chain-id
               :token-id               token-id
               :contract-address       contract-address
               :test-networks-enabled? (get-in db [:profile/profile :test-networks-enabled?])})]
     {:fx [[:dispatch
            [:hide-bottom-sheet]]
           [:dispatch-later
            {:ms       600
             :dispatch [:wallet/trigger-share-collectible
                        {:title title
                         :uri   uri}]}]]})))

(rf/reg-event-fx
 :wallet/navigate-to-opensea
 (fn [{:keys [db]} [chain-id token-id contract-address]]
   {:fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch
          [:browser.ui/open-url
           (collectible-utils/get-opensea-collectible-url
            {:chain-id               chain-id
             :token-id               token-id
             :contract-address       contract-address
             :test-networks-enabled? (get-in db [:profile/profile :test-networks-enabled?])})]]]}))
