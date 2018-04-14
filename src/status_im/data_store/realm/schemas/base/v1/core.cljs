(ns status-im.data-store.realm.schemas.base.v1.core
  (:require [status-im.data-store.realm.schemas.base.v1.network :as network]
            [status-im.data-store.realm.schemas.base.v1.account :as account]
            [taoensso.timbre :as log]))

(def schema [network/schema
             account/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating base database v1: " old-realm new-realm))
