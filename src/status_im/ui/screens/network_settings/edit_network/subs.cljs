(ns status-im.ui.screens.network-settings.edit-network.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-manage-network
 :<- [:get :networks/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-network-valid?
 :<- [:get-manage-network]
 (fn [manage]
   (not-any? :error (vals manage))))
