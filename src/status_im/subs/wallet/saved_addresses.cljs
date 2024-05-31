(ns status-im.subs.wallet.saved-addresses
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/saved-addresses
 :<- [:wallet]
 :-> :saved-addresses)

(rf/reg-sub
 :wallet/address-saved?
 :<- [:wallet]
 (fn [wallet [address]]
   (some #(= address (:address %))
         (:saved-addresses wallet))))

(rf/reg-sub
 :wallet/saved-addresses-by-network-mode
 :<- [:wallet/saved-addresses]
 :<- [:profile/test-networks-enabled?]
 (fn [[saved-addresses test-networks-enabled?]]
   (vals (get saved-addresses (if test-networks-enabled? :test :prod)))))

(rf/reg-sub
 :wallet/grouped-saved-addresses
 :<- [:wallet/saved-addresses-by-network-mode]
 (fn [saved-addresses]
   (->> saved-addresses
        (sort-by :name)
        (group-by #(string/upper-case (first (:name %))))
        (map (fn [[k v]]
               {:title k
                :data  v})))))
