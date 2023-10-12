(ns status-im2.subs.wallet-2.wallet
  (:require [re-frame.core :as re-frame]
            [status-im2.contexts.wallet.common.utils :as utils]))

(re-frame/reg-sub
 :wallet-2/balances
 :<- [:profile/wallet-accounts]
 :<- [:wallet-2/tokens]
 (fn [[accounts tokens]]
   (for [account accounts]
     (let [address (:address account)]
       {:address address
        :balance (utils/calculate-balance address tokens)}))))
