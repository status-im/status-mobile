(ns status-im.data-store.realm.schemas.account.v1.contact-group
  (:require [taoensso.timbre :as log]))

(def schema {:name       :contact-group
             :primaryKey :group-id
             :properties {:group-id         :string
                          :name             :string
                          :timestamp        :int
                          :order            :int 
                          :contacts         {:type    "string[]"}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating group schema v1"))
