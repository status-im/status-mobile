(ns status-im.subs.wallet.saved-addresses
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/saved-address
 :<- [:wallet/ui]
 :-> :saved-address)

(rf/reg-sub
 :wallet/saved-addresses
 :<- [:wallet]
 :-> :saved-addresses)

(rf/reg-sub
 :wallet/saved-addresses-by-network-mode
 :<- [:wallet/saved-addresses]
 :<- [:profile/test-networks-enabled?]
 (fn [[saved-addresses test-networks-enabled?]]
   (get saved-addresses (if test-networks-enabled? :test :prod))))

(rf/reg-sub
 :wallet/address-saved?
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses [_ address]]
   (contains? saved-addresses address)))

(rf/reg-sub
 :wallet/has-saved-addresses?
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses]
   (-> saved-addresses vals boolean)))

(rf/reg-sub
 :wallet/grouped-saved-addresses
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses]
   (->> saved-addresses
        vals
        (sort-by :name)
        (group-by #(string/upper-case (first (:name %))))
        (map (fn [[k v]]
               {:title k
                :data  v})))))

(rf/reg-sub
 :wallet/saved-addresses-addresses
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses]
   (-> saved-addresses keys set)))

(rf/reg-sub
 :wallet/saved-address-by-address
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses [_ address]]
   (get saved-addresses address)))

(rf/reg-sub
 :wallet/filtered-saved-addresses
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses [_ query]]
   (->> saved-addresses
        vals
        (sort-by :name)
        (filter
         (fn [{:keys [name address ens chain-short-names]}]
           (let [lowercase-query (string/lower-case query)]
             (or
              (string/includes? (string/lower-case name) lowercase-query)
              (string/includes? address lowercase-query)
              (string/includes? ens lowercase-query)
              (string/includes? chain-short-names lowercase-query))))))))
