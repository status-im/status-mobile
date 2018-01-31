(ns status-im.ui.screens.accounts.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub :get-current-public-key
  (fn [db]
    (:current-public-key db)))

(reg-sub :get-accounts :accounts/accounts)

(reg-sub :get-current-account :accounts/account)
