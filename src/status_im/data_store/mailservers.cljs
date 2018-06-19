(ns status-im.data-store.mailservers
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   (assoc cofx :data-store/mailservers (-> @core/account-realm
                                           (core/get-all :mailserver)
                                           (core/all-clj :mailserver)))))

(defn save-tx
  "Returns tx function for saving a mailserver"
  [{:keys [id] :as mailserver}]
  (fn [realm]
    (core/create realm
                 :mailserver
                 mailserver
                 true)))

(defn delete-tx
  "Returns tx function for deleting a mailserver"
  [id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :mailserver :id id))))
