(ns status-im.ui.screens.accounts.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :get-current-public-key
  (fn [db]
    (:current-public-key db)))

(reg-sub :get-accounts
  (fn [db]
    (:accounts/accounts db)))

(reg-sub :get-current-account-id
  (fn [db]
    (:accounts/current-account-id db)))

(reg-sub :get-current-account
  :<- [:get-current-account-id]
  :<- [:get-accounts]
  (fn [[account-id accounts]]
    (some-> accounts (get account-id))))
