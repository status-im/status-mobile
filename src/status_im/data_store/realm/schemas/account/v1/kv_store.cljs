(ns status-im.data-store.realm.schemas.account.v1.kv-store
  (:require [taoensso.timbre :as log]))

(def schema {:name       :kv-store
             :primaryKey :key
             :properties {:key   "string"
                          :value "string"}})

(defn migration [_ _]
  (log/debug "migrating kv-store schema"))
