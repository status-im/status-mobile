(ns status-im.data-store.realm.schemas.account.v1.tag
  (:require [taoensso.timbre :as log]))

(def schema {:name       :tag
             :primaryKey :name
             :properties {:name  "string"
                          :count {:type "int" :optional true :default 0}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating tag schema"))