(ns status-im.data-store.realm.schemas.base.v4.core
  (:require [status-im.data-store.realm.schemas.base.v4.network :as network]
            [status-im.data-store.realm.schemas.base.v4.account :as account]
            [status-im.data-store.realm.schemas.base.v1.kv-store :as kv-store]
            [taoensso.timbre :as log]))

(def schema [network/schema
             account/schema
             kv-store/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v4 base database: " old-realm new-realm)
  (network/migration old-realm new-realm)
  (account/migration old-realm new-realm))


