(ns status-im.ui.screens.accounts.db
  (:require [cljs.spec.alpha :as s]))

;;{id (string) account (map)} all created accounts
(s/def :accounts/accounts (s/nilable map?))
;;true during creating new account
(s/def :accounts/account-creation? (s/nilable boolean?))
;;what is the difference ? ^
(s/def :accounts/creating-account? (s/nilable boolean?))
;;id of logged in account
(s/def :accounts/current-account-id (s/nilable string?))
;;used during recovering account
(s/def :accounts/recover (s/nilable map?))
;;used during logging
(s/def :accounts/login (s/nilable map?))