(ns status-im.data-store.realm.schemas.base.v2.core
  (:require [status-im.data-store.realm.schemas.base.v2.account :as account]
            [status-im.data-store.realm.schemas.base.v1.kv-store :as kv-store]
            [taoensso.timbre :as log]))

(def schema [account/schema
             kv-store/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v2 base database: " old-realm new-realm)
  (account/migration old-realm new-realm)
  (kv-store/migration old-realm new-realm))

