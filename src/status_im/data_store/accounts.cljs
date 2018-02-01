(ns status-im.data-store.accounts
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.accounts :as data-store]))

(defn get-all []
  (data-store/get-all-as-list))

(defn get-by-address [address]
  (data-store/get-by-address address))

(defn save [account update?]
  (data-store/save account update?))

(re-frame/reg-fx
  :data-store.accounts/save
  (fn [account]
    (save account true)))

(re-frame/reg-cofx
 :data-store/accounts
 (fn [coeffects _]
   (assoc coeffects :all-accounts (get-all))))
