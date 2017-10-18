(ns status-im.data-store.realm.accounts
  (:require [status-im.data-store.realm.core :as realm]))

(defn- reformat-networks [account]
  (update account :networks
          (fn [networks]
            (into {}
                  (map (fn [{:keys [id] :as network}]
                         [id network])
                       (vals (js->clj networks :keywordize-keys true)))))))

(defn get-all []
  (realm/get-all realm/base-realm :account))

(defn get-all-as-list []
  (map reformat-networks (realm/realm-collection->list (get-all))))

(defn get-by-address [address]
  (reformat-networks
    (realm/get-one-by-field-clj realm/base-realm :account :address address)))

(defn save [account update?]
  (realm/write realm/base-realm
    (let [account' (update account :networks (comp vec vals))]
      #(realm/create realm/base-realm :account account' update?))))
