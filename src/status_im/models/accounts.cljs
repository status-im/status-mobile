(ns status-im.models.accounts
  (:require [status-im.persistence.realm :as r]))

(defn get-accounts []
      (-> (r/get-all :accounts)
          r/collection->map))

(defn create-account [{:keys [address public-key] :as account}]
      (->> account
           (r/create :accounts)))

(defn save-accounts [accounts]
      (r/write #(mapv create-account accounts)))


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn accounts-list []
      (r/get-all :accounts))

(defn account-by-address [address]
      (r/single-cljs (r/get-by-field :accounts :address address)))
