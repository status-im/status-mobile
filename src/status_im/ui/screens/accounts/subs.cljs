(ns status-im.ui.screens.accounts.subs
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.accounts.access.core :as accounts.access]
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
   (let [{:keys [step password password-confirm name]} create]
     (or (and password (= :enter-password step) (spec/valid? ::db/password password))
         (and password-confirm (= :confirm-password step) (spec/valid? ::db/password password-confirm))
         (and name (= :enter-name step) (not (string/blank? name)))))))

(re-frame/reg-sub
 :get-account-access-next-enabled?
 (fn [{:accounts/keys [access]}]
   (let [{:keys [step passphrase password password-confirm]} access]
     (or (and passphrase (= :passphrase step) (not (accounts.access/check-phrase-errors passphrase)))
         (and password (= :enter-password step) (spec/valid? ::db/password password))
         (and password-confirm (= :confirm-password step) (spec/valid? ::db/password password-confirm))))))

(re-frame/reg-sub
 :get-access-account
 (fn [db]
   (:accounts/access db)))
