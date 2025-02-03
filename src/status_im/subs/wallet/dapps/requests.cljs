(ns status-im.subs.wallet.dapps.requests
  (:require [re-frame.core :as rf]
            [status-im.contexts.keycard.utils :as keycard]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.typed-data :as typed-data]
            [utils.string]))

(rf/reg-sub
 :wallet-connect/current-request-address
 :<- [:wallet-connect/current-request]
 :-> :address)

(rf/reg-sub
 :wallet-connect/current-request-display-data
 :<- [:wallet-connect/current-request]
 :-> :display-data)

(rf/reg-sub
 :wallet-connect/current-request-method
 :<- [:wallet-connect/current-request]
 (fn [request]
   (-> request
       :event
       data-store/get-request-method)))

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
 :wallet-connect/current-request-dapp
 :<- [:wallet-connect/current-request]
 :<- [:wallet-connect/sessions]
 (fn [[request sessions]]
   (data-store/get-current-request-dapp request sessions)))

(rf/reg-sub
 :wallet-connect/chain-id
 :<- [:wallet-connect/current-request]
 (fn [request]
   (-> request
       (get-in [:event :params :chainId])
       (networks/eip155->chain-id))))

(rf/reg-sub
 :wallet-connect/current-request-network
 :<- [:wallet-connect/chain-id]
 networks/chain-id->network-details)

(rf/reg-sub
 :wallet-connect/typed-data-request?
 :<- [:wallet-connect/current-request-method]
 typed-data/typed-data-request?)

(rf/reg-sub
 :wallet-connect/sign-on-keycard?
 :<- [:wallet/keypairs]
 :<- [:wallet-connect/current-request-address]
 (fn [[keypairs address]]
   (keycard/keycard-address? keypairs address)))
