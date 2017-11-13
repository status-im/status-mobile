(ns status-im.data-store.realm.accounts
  (:require [status-im.data-store.realm.core :as realm]))

(defn get-all []
  (realm/get-all realm/base-realm :account))

(defn get-all-as-list []
  (->> (get-all)
       realm/js-object->clj
       (mapv #(realm/fix-map % :networks :id))))

(defn get-by-address [address]
  (realm/fix-map (realm/get-one-by-field-clj realm/base-realm :account :address address)
                 :networks :id))

(defn save [account update?]
  (realm/write realm/base-realm
               (let [account' (realm/fix-map->vec account :networks)]
                 #(realm/create realm/base-realm :account account' update?))))
