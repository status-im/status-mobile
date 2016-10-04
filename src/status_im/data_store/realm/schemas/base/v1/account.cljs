(ns status-im.data-store.realm.schemas.base.v1.account
  (:require [taoensso.timbre :as log]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address             "string"
                          :public-key          "string"
                          :updates-public-key  {:type     :string
                                                :optional true}
                          :updates-private-key {:type     :string
                                                :optional true}
                          :name                {:type "string" :optional true}
                          :phone               {:type "string" :optional true}
                          :email               {:type "string" :optional true}
                          :status              {:type "string" :optional true}
                          :photo-path          "string"
                          :last-updated        {:type "int" :default 0}
                          :signed-up?          {:type    :bool
                                                :default false}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating account schema"))