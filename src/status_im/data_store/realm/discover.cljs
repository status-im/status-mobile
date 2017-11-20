(ns status-im.data-store.realm.discover
  (:require [status-im.data-store.realm.core :as realm]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  [ordering]
  (-> @realm/account-realm
      (realm/get-all :discover)
      (realm/sorted :created-at ordering)))

(defn get-all-as-list
  [ordering]
  (realm/js-object->clj (get-all ordering)))

(defn save
  [discover]
  (realm/write @realm/account-realm
               #(realm/create @realm/account-realm :discover discover true)))

(defn save-all
  [discoveries]
  (realm/write @realm/account-realm
               (fn []
                 (doseq [discover discoveries]
                   (realm/create @realm/account-realm :discover discover true)))))

(defn delete
  [by ordering max-count]
  (let [discoveries  (realm/get-all @realm/account-realm :discover)
        count (realm/get-count discoveries)]
    (if (> count max-count)
      (let [to-delete (-> discoveries
                          (realm/sorted by ordering)
                          (realm/page 0 (- max-count count)))]
        (realm/write @realm/account-realm
                     (fn []
                       (log/debug (str "Deleting " (realm/get-count to-delete) " discoveries"))
                       (realm/delete @realm/account-realm to-delete)))))))
