(ns status-im.data-store.accounts
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.accounts :as data-store]))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-by-address [address]
  (data-store/get-by-address address))

(re-frame/reg-cofx
  :data-store/get-all-accounts
  (fn [coeffects _]
    (assoc coeffects :all-accounts (data-store/get-all-as-list))))

(re-frame/reg-fx
  :data-store/save-account
  (fn [{:keys [after-update-event] :as account}]
    (let [account-to-save (dissoc account :after-update-event)]
      (async/go (async/>! core/realm-queue #(if after-update-event
                                              (do (data-store/save account-to-save true)
                                                  (re-frame/dispatch after-update-event))
                                              (data-store/save account-to-save true)))))))

