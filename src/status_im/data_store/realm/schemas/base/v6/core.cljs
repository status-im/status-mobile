(ns status-im.data-store.realm.schemas.base.v6.core
  (:require [status-im.data-store.realm.schemas.base.v4.network :as network]
            [status-im.data-store.realm.schemas.base.v6.account :as account]
            [taoensso.timbre :as log]))

(def schema [network/schema
             account/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating v6 base database: " old-realm new-realm)
  (account/migration old-realm new-realm))
