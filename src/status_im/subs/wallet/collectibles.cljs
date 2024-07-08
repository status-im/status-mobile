(ns status-im.subs.wallet.collectibles
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]))

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
     animation-media-type  :animation-media-type} :collectible-data
    {collection-image-url :image-url}             :collection-data}]
  (cond
    (svg-animation? animation-url animation-media-type)
    {:uri  animation-url
     :svg? true}

    (animation? animation-url animation-media-type)
    {:uri animation-url}

    (not (string/blank? collectible-image-url))
    {:uri collectible-image-url}

    :else
    {:uri collection-image-url}))

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
 :wallet/all-collectibles-list
 :<- [:wallet]
 (fn [{:keys [accounts]}]
   (let [max-collectibles (->> accounts
                               (map (comp count :collectibles val))
                               (apply max))
         all-collectibles (map (fn [[_address {:keys [collectibles]}]]
                                 (let [amount-to-add      (- max-collectibles (count collectibles))
                                       empty-collectibles (repeat amount-to-add nil)]
                                   (reduce conj collectibles empty-collectibles)))
                               accounts)]
     (->> all-collectibles
          (apply interleave)
          (remove nil?)
          (add-collectibles-preview-url)))))

(re-frame/reg-sub
 :wallet/all-collectibles-list-in-selected-networks
 :<- [:wallet/all-collectibles-list]
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
 :wallet/last-collectible-details
 :<- [:wallet]
 (fn [wallet]
   (let [last-collectible (:last-collectible-details wallet)]
     (assoc last-collectible :preview-url (preview-url last-collectible)))))

(re-frame/reg-sub
 :wallet/last-collectible-aspect-ratio
 :<- [:wallet]
 (fn [wallet]
   (:last-collectible-aspect-ratio wallet)))

(re-frame/reg-sub
 :wallet/last-collectible-details-chain-id
 :<- [:wallet/last-collectible-details]
 (fn [collectible]
   (get-in collectible [:id :contract-id :chain-id])))

(re-frame/reg-sub
 :wallet/last-collectible-details-traits
 :<- [:wallet/last-collectible-details]
 (fn [collectible]
   (get-in collectible [:collectible-data :traits])))

(re-frame/reg-sub
 :wallet/last-collectible-details-owner
 :<- [:wallet/last-collectible-details]
 :<- [:wallet]
 (fn [[collectible wallet]]
   (let [address (:address (first (:ownership collectible)))
         account (get-in wallet [:accounts address])]
     account)))
