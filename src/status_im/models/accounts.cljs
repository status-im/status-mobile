(ns status-im.models.accounts
  (:require [status-im.persistence.realm.core :as r]))

(defn get-accounts []
  (-> (r/get-all :base :accounts)
      r/collection->map))

(defn save-account [update?]
  #(r/create :base :accounts % update?))

(defn save-accounts [accounts update?]
  (r/write :base #(mapv (save-account update?) accounts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn accounts-list []
  (r/get-all :base :accounts))

(defn account-by-address [address]
  (r/single-cljs (r/get-by-field :base :accounts :address address)))
