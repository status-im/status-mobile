(ns status-im.subs.wallet.dapps.requests
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
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
   (let [dapp-url (get-in request [:event :verifyContext :verified :origin])]
     (->> sessions
          (filter (fn [session]
                    (= (utils.string/remove-trailing-slash dapp-url)
                       (utils.string/remove-trailing-slash (get session :url)))))
          (first)))))

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
