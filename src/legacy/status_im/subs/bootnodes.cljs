(ns legacy.status-im.subs.bootnodes
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :custom-bootnodes/enabled?
 :<- [:profile/profile]
 :<- [:networks/current-network]
 (fn [[{:keys [custom-bootnodes-enabled?]} current-network]]
   (get custom-bootnodes-enabled? current-network)))

(re-frame/reg-sub
 :custom-bootnodes/network-bootnodes
 :<- [:profile/profile]
 :<- [:networks/current-network]
 (fn [[multiaccount current-network]]
   (get-in multiaccount [:custom-bootnodes current-network])))

(re-frame/reg-sub
 :get-manage-bootnode
 :<- [:bootnodes/manage]
 (fn [manage]
   manage))

(re-frame/reg-sub
 :wakuv2-nodes/validation-errors
 :<- [:wakuv2-nodes/manage]
 (fn [manage]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         manage))))
