(ns status-im.subs.wallet.wallet-connect
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]))

(rf/reg-sub
 :wallet-connect/current-request-address
 :<- [:wallet-connect/current-request]
 :-> :address)

(rf/reg-sub
 :wallet-connect/current-request-display-data
 :<- [:wallet-connect/current-request]
 :-> :display-data)

(rf/reg-sub
 :wallet-connect/account-details-by-address
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts [_ address]]
   (let [{:keys [customization-color name emoji]} (wallet-utils/get-account-by-address accounts address)]
     {:customization-color customization-color
      :name                name
      :emoji               emoji})))

(rf/reg-sub
 :wallet-connect/current-request-account-details
 :<- [:wallet-connect/current-request-address]
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [[address accounts]]
   (let [{:keys [customization-color name emoji]} (wallet-utils/get-account-by-address accounts address)]
     {:customization-color customization-color
      :name                name
      :emoji               emoji})))

(rf/reg-sub
 :wallet-connect/session-proposer
 :<- [:wallet-connect/current-proposal]
 (fn [proposal]
   (-> proposal :params :proposer)))

(rf/reg-sub
 :wallet-connect/session-proposer-name
 :<- [:wallet-connect/session-proposer]
 (fn [proposer]
   (-> proposer :metadata :name)))
