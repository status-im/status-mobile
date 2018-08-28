(ns status-im.models.account
  (:require [status-im.ui.screens.accounts.utils :as accounts.utils]))

(defn logged-in? [cofx]
  (boolean
   (get-in cofx [:db :account/account])))

(defn  update-sign-in-time
  [{db :db now :now :as cofx}]
  (accounts.utils/account-update {:last-sign-in now} cofx))
