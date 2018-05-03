(ns status-im.data-store.local-storage
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
  :data-store/get-local-storage-data
  (fn [cofx _]
    (assoc cofx :get-local-storage-data #(-> @core/account-realm
                                             (core/get-by-field :local-storage :chat-id %)
                                             (core/single-clj :local-storage)
                                             :data))))

(defn set-local-storage-data-tx
  "Returns tx function setting local storage data"
  [data]
  (fn [realm]
    (core/create realm :local-storage data true)))
