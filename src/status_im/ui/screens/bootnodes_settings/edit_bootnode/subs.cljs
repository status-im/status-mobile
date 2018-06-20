(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-manage-bootnode
 :<- [:get :bootnodes/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-bootnode-valid?
 :<- [:get-manage-bootnode]
 (fn [manage]
   (not-any? :error (vals manage))))
