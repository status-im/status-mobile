(ns status-im.data-store.realm.schemas.base.v12.core
  (:require [status-im.data-store.realm.schemas.base.v1.network :as network]
            [status-im.data-store.realm.schemas.base.v4.bootnode :as bootnode]
            [status-im.data-store.realm.schemas.base.v11.account :as account]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [taoensso.timbre :as log]))

(def schema [network/schema
             bootnode/schema
             account/schema])

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v11")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (aset (aget accounts i) "signing-phrase" (signing-phrase/generate))
      (aset (aget accounts i) "wallet-set-up-passed?" false))))
