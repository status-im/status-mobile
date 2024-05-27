(ns status-im.subs.wallet.buy
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/crypto-on-ramps
 :<- [:wallet]
 :-> :crypto-on-ramps)
