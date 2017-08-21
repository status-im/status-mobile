(ns status-im.ui.screens.network-settings.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
  :get-current-account-network
  :<- [:get-current-account]
  :<- [:get :networks/networks]
  (fn [[current-account networks]]
    (get networks (:network current-account))))