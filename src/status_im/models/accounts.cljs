(ns status-im.models.accounts
  (:require [status-im.persistence.realm.core :as r]))

(defn get-accounts []
      (-> (r/get-all :base :accounts)
          r/collection->map))

(defn create-account [{:keys [address public-key] :as account}]
      (->> account
           (r/create :base :accounts)))

(defn save-accounts [accounts]
      (r/write :base #(mapv create-account accounts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn accounts-list []
      (r/get-all :base :accounts))

(defn account-by-address [address]
      (r/single-cljs (r/get-by-field :base :accounts :address address)))
