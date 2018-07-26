
(ns status-im.data-store.realm.schemas.base.v8.account
  (:require [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [clojure.set :as set]
            [status-im.data-store.realm.schemas.base.v4.account :as v7]))

(def schema v7/schema)

(def removed-tokens
  #{:ATT})

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v8")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (update-in [:wallet :visible-tokens :testnet]
                                        #(set/difference % removed-tokens)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))
