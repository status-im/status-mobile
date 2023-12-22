(ns status-im.contexts.wallet.events.collectibles
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [clojure.string :as string]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [utils.ethereum.chain :as chain]
            [utils.transforms :as types]))

(def collectibles-request-batch-size 1000)

(defn displayable-collectible?
  [collectible]
  (let [{:keys [image-url animation-url]} (:collectible-data collectible)]
    (or (not (string/blank? animation-url))
        (not (string/blank? image-url)))))

(defn- add-collectibles-to-accounts
  [accounts collectibles]
  (reduce (fn [acc {:keys [ownership] :as collectible}]
            (->> ownership
                 (map :address) ; In ERC1155 tokens a collectible can be owned by multiple addresses.
                 (reduce (fn add-collectible-to-address [acc address]
                           (update-in acc [address :collectibles] conj collectible))
                         acc)))
          accounts
          collectibles))

(defn store-collectibles
  [{:keys [db]} [collectibles]]
  (let [displayable-collectibles (filter displayable-collectible? collectibles)]
    {:db (update-in db [:wallet :accounts] add-collectibles-to-accounts displayable-collectibles)}))

(rf/reg-event-fx :wallet/store-collectibles store-collectibles)

(defn clear-stored-collectibles
  [{:keys [db]}]
  {:db (update-in db [:wallet :accounts] update-vals #(dissoc % :collectibles))})

(rf/reg-event-fx :wallet/clear-stored-collectibles clear-stored-collectibles)

(defn store-last-collectible-details
  [{:keys [db]} [collectible]]
  {:db (assoc-in db [:wallet :last-collectible-details] collectible)})

(rf/reg-event-fx :wallet/store-last-collectible-details store-last-collectible-details)

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

(rf/reg-event-fx
 :wallet/request-collectibles
 (fn [{:keys [db]} [{:keys [start-at-index new-request?]}]]
   (let [request-id          0
         collectibles-filter nil
         data-type           (collectible-data-types :header)
         fetch-criteria      {:fetch-type            (fetch-type :fetch-if-not-cached)
                              :max-cache-age-seconds max-cache-age-seconds}
         request-params      [request-id
                              [(chain/chain-id db)]
                              (keys (get-in db [:wallet :accounts]))
                              collectibles-filter
                              start-at-index
                              collectibles-request-batch-size
                              data-type
                              fetch-criteria]]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getOwnedCollectiblesAsync"
              :params     request-params
              :on-success (fn [])
              :on-error   (fn [error]
                            (log/error "failed to request collectibles"
                                       {:event  :wallet/request-collectibles
                                        :error  error
                                        :params request-params}))}]]
           (when new-request?
             [:dispatch [:wallet/clear-stored-collectibles]])]})))

(rf/reg-event-fx
 :wallet/owned-collectibles-filtering-done
 (fn [_ [{:keys [message]}]]
   (let [{:keys [has-more offset
                 collectibles]} (cske/transform-keys csk/->kebab-case-keyword (types/json->clj message))
         start-at-index         (+ offset (count collectibles))]
     {:fx [[:dispatch [:wallet/store-collectibles collectibles]]
           (when has-more
             [:dispatch [:wallet/request-collectibles {:start-at-index start-at-index}]])]})))

(rf/reg-event-fx
 :wallet/get-collectible-details
 (fn [_ [collectible-id]]
   (let [request-id               0
         collectible-id-converted (cske/transform-keys csk/->PascalCaseKeyword collectible-id)
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
   (let [response               (cske/transform-keys csk/->kebab-case-keyword
                                                     (types/json->clj message))
         {:keys [collectibles]} response
         collectible            (first collectibles)]
     (if collectible
       {:fx [[:dispatch [:wallet/store-last-collectible-details collectible]]]}
       (log/error "failed to get collectible details"
                  {:event    :wallet/get-collectible-details-done
                   :response response})))))
