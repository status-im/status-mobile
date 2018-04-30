(ns status-im.data-store.accounts
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-by-address [address]
  (-> @core/base-realm
      (core/get-by-field :account :address address)
      (core/single-clj :account)
      (update :settings core/deserialize)))

(re-frame/reg-cofx
  :data-store/get-all-accounts
  (fn [coeffects _]
    (assoc coeffects :all-accounts (-> @core/base-realm
                                       (core/get-all :account)
                                       (core/all-clj :account)
                                       (as-> accounts
                                           (map #(update % :settings core/deserialize) accounts))))))

(defn save-account-tx
  "Returns tx function for saving account"
  [{:keys [after-update-event] :as account}]
  (fn [realm]
    (let [account-to-save (-> account
                              (dissoc :after-update-event)
                              (update :settings core/serialize)
                              (update :networks vals))]
      (core/create realm :account account-to-save true)
      (when after-update-event
        (re-frame/dispatch after-update-event)))))
