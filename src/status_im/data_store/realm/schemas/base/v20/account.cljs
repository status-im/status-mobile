(ns status-im.data-store.realm.schemas.base.v20.account
  (:require [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [clojure.set :as set]
            [status-im.utils.random :as random]
            [status-im.data-store.realm.schemas.base.v8.account :as v8]))

(def schema
  (assoc-in v8/schema
            [:properties :installation-id]
            {:type :string}))

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v8")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)]
        (when-not (aget account "installation-id")
          (aset account "installation-id" (random/guid)))))))
