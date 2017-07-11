(ns status-im.accounts.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :accounts/accounts (s/nilable map?))                                     ;;{id (string) account (map)} all created accounts
(s/def :accounts/account-creation? (s/nilable boolean?))            ;;true during creating new account
(s/def :accounts/creating-account? (s/nilable boolean?))                        ;;what is the difference ? ^
(s/def :accounts/current-account-id (s/nilable string?))            ;;id of logged in account
(s/def :accounts/recover (s/nilable map?))                          ;;used during recovering account
(s/def :accounts/login (s/nilable map?))                                        ;;used during logging