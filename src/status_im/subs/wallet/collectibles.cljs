(ns status-im.subs.wallet.collectibles
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]))

(defn- svg-animation?
  [url media-type]
  (and (not (string/blank? url))
       (string/includes? media-type "svg")))

(defn- preview-url
  [{{collectible-image-url :image-url
     animation-url         :animation-url
     animation-media-type  :animation-media-type} :collectible-data
    {collection-image-url :image-url}             :collection-data}]
  (cond
    (svg-animation? animation-url animation-media-type)
    {:uri  animation-url
     :svg? true}

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
 :wallet/all-collectibles
 :<- [:wallet]
 (fn [wallet]
   (->> wallet
        :accounts
        (mapcat (comp :collectibles val))
        (add-collectibles-preview-url))))

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
