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
        :saved-addressesj
        (some #(= address (:address %)))
        boolean)))

(rf/reg-sub
 :wallet/saved-address-by-address
 :<- [:wallet]
 (fn [wallet [_ address]]

   (prn {:saved (:saved-addresses wallet)
         :needle address
         :found (filter #(= address (:address %)) (:saved-addresses wallet))
         :return (first (filter #(= address (:address %)) (:saved-addresses wallet)))
         :f-return (->> wallet
                        :saved-addresses
                        (filter #(= address (:address %)))
                        first)
         })
   (->> wallet
        :saved-addresses
        (filter #(= address (:address %)))
        first)))
