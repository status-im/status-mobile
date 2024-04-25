(ns status-im.subs.wallet.saved-addresses
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/saved-addresses
 :<- [:wallet]
 :-> :saved-addresses)

(rf/reg-sub
 :wallet/saved-addresses?
 :<- [:wallet]
 (fn [{:keys [saved-addresses]}]
   (-> saved-addresses seq boolean)))

(rf/reg-sub
 :wallet/address-saved?
 :<- [:wallet]
 (fn [wallet [address]]
   (some #(= address (:address %))
         (:saved-addresses wallet))))
