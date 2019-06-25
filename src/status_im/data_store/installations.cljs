(ns status-im.data-store.installations
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/get-all-installations
 (fn [coeffects _]
   (assoc coeffects :all-installations (-> @core/account-realm
                                           (core/get-all :installation)
                                           (core/all-clj :installation)))))

(defn save
  "Returns tx function for saving a installation"
  [installation]
  (fn [realm]
    (core/create realm
                 :installation
                 installation
                 true)))

(defn enable
  [installation-id]
  (save {:installation-id installation-id
         :enabled? true}))

(defn disable
  [installation-id]
  (save {:installation-id installation-id
         :enabled? false}))

(defn delete
  "Returns tx function for deleting an installation"
  [id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :installation :installation-id (name id)))))

