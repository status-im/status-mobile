(ns status-im.subs.wallet.dapps.core
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            status-im.subs.wallet.dapps.proposals
            status-im.subs.wallet.dapps.requests
            status-im.subs.wallet.dapps.sessions
            status-im.subs.wallet.dapps.transactions
            [utils.string]))

(rf/reg-sub
 :wallet-connect/account-details-by-address
 :<- [:wallet/accounts-without-watched-accounts]
 (fn [accounts [_ address]]
   (let [{:keys [customization-color name emoji]} (wallet-utils/get-account-by-address accounts address)]
     {:customization-color customization-color
      :name                name
      :emoji               emoji})))
