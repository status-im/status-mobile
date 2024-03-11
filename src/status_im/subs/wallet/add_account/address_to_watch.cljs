(ns status-im.subs.wallet.add-account.address-to-watch
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/add-address-to-watch
 :<- [:wallet/ui]
 :-> :add-address-to-watch)

(rf/reg-sub
 :wallet/watch-address-activity-state
 :<- [:wallet/add-address-to-watch]
 :-> :activity-state)

(rf/reg-sub
 :wallet/watch-address-validated-address
 :<- [:wallet/add-address-to-watch]
 :-> :validated-address)
