(ns status-im.data-store.dapp-permissions
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/all-dapp-permissions
 (fn [cofx _]
   (assoc cofx :all-dapp-permissions (-> @core/account-realm
                                         (core/get-all :dapp-permissions)
                                         (core/all-clj :dapp-permissions)))))

(defn save-dapp-permissions
  "Returns tx function for saving dapp permissions"
  [permissions]
  (fn [realm]
    (core/create realm :dapp-permissions permissions true)))