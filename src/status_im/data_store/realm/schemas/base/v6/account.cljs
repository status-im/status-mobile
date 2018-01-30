(ns status-im.data-store.realm.schemas.base.v6.account
  (:require [taoensso.timbre :as log]
            [status-im.constants :as constants]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address             :string
                          :public-key          :string
                          :updates-public-key  {:type     :string
                                                :optional true}
                          :updates-private-key {:type     :string
                                                :optional true}
                          :name                {:type :string :optional true}
                          :email               {:type :string :optional true}
                          :status              {:type :string :optional true}
                          :debug?              {:type :bool :default false}
                          :photo-path          :string
                          :signing-phrase      {:type :string}
                          :last-updated        {:type :int :default 0}
                          :signed-up?          {:type    :bool
                                                :default false}
                          :network             :string
                          :networks            {:type       :list
                                                :objectType :network}
                          :wnode               :string
                          :settings            {:type :string}}})

(defn migration [_old-realm new-realm]
  (log/debug "migrating account schema v6")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account (aget accounts i)]
        (aset account "wnode" constants/default-wnode)))))
