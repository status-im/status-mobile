(ns status-im.subs.wallet.collectibles
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [utils.collection]))

(defn- filter-collectibles-in-chains
  [collectibles chain-ids]
  (filter #(contains? chain-ids (get-in % [:id :contract-id :chain-id])) collectibles))

(defn- svg-animation?
  [url media-type]
  (and (not (string/blank? url))
       (string/includes? media-type "svg")))

(defn- animation?
  [url media-type]
  (and (not (string/blank? url))
       (not (string/blank? media-type))))

(defn- preview-url
  [{{collectible-image-url :image-url
     animation-url         :animation-url
     animation-media-type  :animation-media-type} :collectible-data}]
  (cond
    (svg-animation? animation-url animation-media-type)
    {:uri  animation-url
     :svg? true}

    (animation? animation-url animation-media-type)
    {:uri animation-url}

    (not (string/blank? collectible-image-url))
    {:uri collectible-image-url}

    :else
    {:uri nil}))

(defn add-collectibles-preview-url
  [collectibles]
  (map (fn [collectible]
         (assoc collectible :preview-url (preview-url collectible)))
       collectibles))

(re-frame/reg-sub
 :wallet/current-viewing-account-collectibles
 :<- [:wallet/current-viewing-account]
 (fn [current-account]
   (-> current-account :collectibles add-collectibles-preview-url)))

(re-frame/reg-sub
 :wallet/current-viewing-account-collectibles-in-selected-networks
 :<- [:wallet/current-viewing-account-collectibles]
 :<- [:wallet/selected-networks->chain-ids]
 (fn [[collectibles chain-ids]]
   (filter-collectibles-in-chains collectibles chain-ids)))

(re-frame/reg-sub
 :wallet/owned-collectibles-list
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts]
   (let [max-collectibles (->> accounts
                               (map (comp count :collectibles))
                               (apply max))
         all-collectibles (map (fn [{:keys [collectibles]}]
                                 (let [amount-to-add      (- max-collectibles (count collectibles))
                                       empty-collectibles (repeat amount-to-add nil)]
                                   (reduce conj collectibles empty-collectibles)))
                               accounts)]
     (->> all-collectibles
          (apply interleave)
          (remove nil?)
          (utils.collection/distinct-by :id)
          (add-collectibles-preview-url)))))

(re-frame/reg-sub
 :wallet/owned-collectibles-list-in-selected-networks
 :<- [:wallet/owned-collectibles-list]
 :<- [:wallet/selected-networks->chain-ids]
 (fn [[all-collectibles chain-ids]]
   (filter-collectibles-in-chains all-collectibles chain-ids)))

(re-frame/reg-sub
 :wallet/current-viewing-account-collectibles-filtered
 :<- [:wallet/current-viewing-account-collectibles]
 (fn [current-account-collectibles [_ search-text]]
   (let [search-text-lower-case (string/lower-case search-text)]
     (filter (fn [{{collection-name :name}  :collection-data
                   {collectible-name :name} :collectible-data}]
               (or (string/includes? (string/lower-case collection-name) search-text-lower-case)
                   (string/includes? (string/lower-case collectible-name) search-text-lower-case)))
             current-account-collectibles))))

(re-frame/reg-sub
 :wallet/collectible
 :<- [:wallet/ui]
 :-> :collectible)

(re-frame/reg-sub
 :wallet/collectible-details
 :<- [:wallet/collectible]
 (fn [collectible]
   (as-> collectible $
     (:details $)
     (assoc $ :preview-url (preview-url $)))))

(re-frame/reg-sub
 :wallet/collectible-aspect-ratio
 :<- [:wallet/collectible]
 (fn [collectible]
   (:aspect-ratio collectible 1)))

(re-frame/reg-sub
 :wallet/collectible-gradient-color
 :<- [:wallet/collectible]
 (fn [collectible]
   (:gradient-color collectible :gradient-1)))

(re-frame/reg-sub
 :wallet/total-owned-collectible
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts [_ ownership address]]
   (let [addresses (if address
                     #{address}
                     (set (map :address accounts)))]
     (reduce (fn [acc item]
               (if (contains? addresses (:address item))
                 (+ acc (js/parseInt (:balance item)))
                 acc))
             0
             ownership))))

(re-frame/reg-sub
 :wallet/collectibles
 :<- [:wallet/ui]
 :-> :collectibles)

(re-frame/reg-sub
 :wallet/collectibles-updating
 :<- [:wallet/collectibles]
 :-> :updating)

(re-frame/reg-sub
 :wallet/current-viewing-account-collectibles-updating?
 :<- [:wallet/collectibles-updating]
 :<- [:wallet/current-viewing-account-address]
 (fn [[updating-addresses address]]
   (contains? updating-addresses address)))

(re-frame/reg-sub
 :wallet/home-tab-collectibles-updating?
 :<- [:wallet/collectibles-updating]
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [[updating-addresses accounts]]
   (->> accounts
        (map :address)
        (every? #(contains? updating-addresses %)))))
