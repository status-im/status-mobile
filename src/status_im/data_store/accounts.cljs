(ns status-im.data-store.accounts
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.utils.types :as types]))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-by-address [address]
  (-> @core/base-realm
      (core/get-by-field :account :address address)
      (core/single-clj :account)
      (update :settings core/deserialize)))

(defn- deserialize-account [account]
  (-> account
      (update :settings core/deserialize)
      (update :networks (partial reduce-kv
                                 (fn [acc network-id props]
                                   (assoc acc network-id
                                          (update props :config types/json->clj)))
                                 {}))))

(re-frame/reg-cofx
 :data-store/get-all-accounts
 (fn [coeffects _]
   (assoc coeffects :all-accounts (-> @core/base-realm
                                      (core/get-all :account)
                                      (core/all-clj :account)
                                      (as-> accounts
                                            (map deserialize-account accounts))))))

(defn- serialize-account [account]
  (-> account
      (update :settings core/serialize)
      (update :networks (partial map (fn [[_ props]]
                                       (update props :config types/clj->json))))))

(defn save-account-tx
  "Returns tx function for saving account"
  [{:keys [after-update-event] :as account}]
  (fn [realm]
    (let [account-to-save (-> (serialize-account account)
                              (dissoc :after-update-event))]
      (core/create realm :account account-to-save true)
      (when after-update-event
        (re-frame/dispatch after-update-event)))))
