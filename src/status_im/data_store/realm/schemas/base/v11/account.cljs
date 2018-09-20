(ns status-im.data-store.realm.schemas.base.v11.account
  (:require [taoensso.timbre :as log]
            [clojure.string :as string]
            [cognitect.transit :as transit]
            [clojure.set :as set]
            [status-im.utils.random :as random]
            [status-im.data-store.realm.schemas.base.v10.account :as v10]))

(def schema
  (assoc-in v10/schema
            [:properties :installation-id]
            {:type :string}))

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v11")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account             (aget accounts i)
            old-installation-id (aget account "installation-id")
            installation-id     (random/guid)]
        (when (string/blank? old-installation-id)
          (aset account "installation-id" installation-id))))))
