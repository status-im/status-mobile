(ns status-im.data-store.realm.schemas.account.v2.group
  (:require [taoensso.timbre :as log]))

(def schema {:name       :group
             :primaryKey :group-id
             :properties {:group-id         :string
                          :name             :string
                          :timestamp        :int
                          :contacts         {:type       :list
                                             :objectType :group-contact}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating group schema v2"))
