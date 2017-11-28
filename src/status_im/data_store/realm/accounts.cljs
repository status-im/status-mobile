(ns status-im.data-store.realm.accounts
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all-as-list []
  (->> (realm/get-all realm/base-realm :account)
       realm/js-object->clj
       (map #(update % :settings realm/deserialize))
       (mapv #(realm/fix-map % :networks :id))))


(defn get-by-address [address]
  (-> (realm/get-one-by-field-clj realm/base-realm :account :address address)
      (update :settings realm/deserialize)
      (realm/fix-map :networks :id)))

(defn- create-account-fn [account update?]
  #(realm/create realm/base-realm :account account update?))

(defn save [account update?]
  (realm/write realm/base-realm
               (-> (realm/fix-map->vec account :networks)
                   (update :settings realm/serialize)
                   (create-account-fn update?))))