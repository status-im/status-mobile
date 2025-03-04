(ns status-im.subs.wallet.routes
  (:require [re-frame.core :as rf]))

(rf/reg-sub :wallet/route
 :<- [:wallet]
 :-> :route)

(rf/reg-sub
 :wallet.routes/best-route
 :<- [:wallet/route]
 :-> :best-route)

(rf/reg-sub
 :wallet.routes/transactions-for-signing
 :<- [:wallet/route]
 :-> :transactions-for-signing)

(rf/reg-sub
 :wallet.routes/state
 :<- [:wallet/route]
 :-> :state)

(rf/reg-sub
 :wallet.routes/uuid
 :<- [:wallet/route]
 :-> :uuid)

(rf/reg-sub
 :wallet.routes/error
 :<- [:wallet/route]
 :-> :error)

(rf/reg-sub
 :wallet.routes/transactions
 :<- [:wallet/route]
 :-> :transactions)

(rf/reg-sub
 :wallet.routes/processing-routes?
 :<- [:wallet.routes/state]
 (fn [state]
   (= state :requesting-routes)))
