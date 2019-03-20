(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-manage-bootnode
 :<- [:get :bootnodes/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-bootnode-validation-errors
 :<- [:get-manage-bootnode]
 (fn [manage]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         manage))))
