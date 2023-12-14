(ns status-im2.subs.wallet.send
  (:require
    [re-frame.core :as rf]
    [utils.number]))

(rf/reg-sub
 :wallet/send-tab
 :<- [:wallet/ui]
 (fn [ui]
   (get-in ui [:send :select-address-tab])))
