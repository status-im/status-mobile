(ns status-im.subs.wallet.saved-addresses
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/saved-addresses
 :<- [:wallet]
 :-> :saved-addresses)

(rf/reg-sub
 :wallet/address-saved?
 :<- [:wallet]
 (fn [wallet [_ address]]
   (->> wallet
        :saved-addresses
        (some #(= address (:address %)))
        boolean)))

(rf/reg-sub
 :wallet/saved-address-by-address
 :<- [:wallet]
 (fn [wallet [_ address]]
   (->> wallet
        :saved-addresses
        (filter #(= address (:address %)))
        first)))
