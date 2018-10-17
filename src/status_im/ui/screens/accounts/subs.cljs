(ns status-im.ui.screens.accounts.subs
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as db]
            [status-im.utils.ethereum.core :as ethereum]
            [cljs.spec.alpha :as spec]))

(re-frame/reg-sub
 :get-current-public-key
 (fn [db]
   (:current-public-key db)))

(re-frame/reg-sub
 :get-accounts
 (fn [db]
   (:accounts/accounts db)))

(re-frame/reg-sub
 :get-current-account
 (fn [db]
   (:account/account db)))

(re-frame/reg-sub
 :get-current-account-hex
 :<- [:get-current-account]
 (fn [{:keys [address]}]
   (ethereum/normalized-address address)))

(re-frame/reg-sub
 :get-account-creation-next-enabled?
 (fn [{:accounts/keys [create]}]
   (db/account-creation-next-enabled? create)))

(re-frame/reg-sub
 :get-recover-account
 (fn [db]
   (:accounts/recover db)))
