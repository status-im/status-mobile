(ns status-im.data-store.realm.accounts
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all-as-list []
  (->> (realm/all-clj (realm/get-all @realm/base-realm :account) :account)
       (mapv #(update % :settings realm/deserialize))))


(defn get-by-address [address]
  (-> @realm/base-realm
      (realm/get-by-field :account :address address)
      (realm/single-clj :account)
      (update :settings realm/deserialize)))

(defn- create-account-fn [account update?]
  #(realm/create @realm/base-realm :account account update?))

(defn save [account update?]
  (realm/write @realm/base-realm
               (-> account
                   (update :settings realm/serialize)
                   (update :networks vals)
                   (create-account-fn update?))))
