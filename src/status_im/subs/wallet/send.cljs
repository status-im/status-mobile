(ns status-im.subs.wallet.send
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/wallet-send
 :<- [:wallet/ui]
 :-> :send)

(rf/reg-sub
 :wallet/send-tab
 :<- [:wallet-send]
 :-> :select-address-tab)
