(ns status-im.data-store.realm.schemas.base.v3.core
  (:require [status-im.data-store.realm.schemas.base.v1.network :as network]
            [status-im.data-store.realm.schemas.base.v3.account :as account]
            [taoensso.timbre :as log]))

(def schema [network/schema
             account/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating base database v3: " old-realm new-realm))
