(ns status-im.accounts.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :accounts/accounts map?)                                     ;;{id (string) account (map)} all created accounts
(s/def :accounts/account-creation? (s/nilable boolean?))            ;;true during creating new account
(s/def :accounts/creating-account? boolean?)                        ;;what is the difference ? ^
(s/def :accounts/current-account-id (s/nilable string?))            ;;id of logged in account
(s/def :accounts/recover (s/nilable map?))                          ;;used during recovering account
(s/def :accounts/login map?)                                        ;;used during logging