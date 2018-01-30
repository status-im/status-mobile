(ns status-im.data-store.realm.schemas.base.v6.account
  (:require [taoensso.timbre :as log]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address             :string
                          :whisper-identity    :string
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
                          :settings            {:type :string}}})


(defn migration [old-realm new-realm]
  (log/debug "migrating account schema v6"))
