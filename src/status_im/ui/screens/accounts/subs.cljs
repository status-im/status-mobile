(ns status-im.ui.screens.accounts.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
  :get-accounts
  (fn [db _]
    (get db :accounts/accounts)))

(reg-sub
  :get-current-account
  :<- [:get :accounts/current-account-id]
  :<- [:get-accounts]
  (fn [[account-id accounts]]
    (some-> accounts (get account-id))))
