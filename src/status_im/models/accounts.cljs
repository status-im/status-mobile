(ns status-im.models.accounts
  (:require [status-im.persistence.realm.core :as r]))

(defn get-accounts []
  (-> (r/get-all :base :account)
      r/realm-collection->list))

(defn save-account [update?]
  #(r/create :base :account % update?))

(defn save-accounts [accounts update?]
  (r/write :base #(mapv (save-account update?) accounts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn accounts-list []
  (r/get-all :base :account))

(defn account-by-address [address]
  (r/single-cljs (r/get-by-field :base :account :address address)))
