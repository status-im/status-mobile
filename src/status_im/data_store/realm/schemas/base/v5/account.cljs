(ns status-im.data-store.realm.schemas.base.v5.account
  (:require [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [clojure.set :as set]
            [status-im.data-store.realm.schemas.base.v4.account :as v4]))

(def schema v4/schema)

(def removed-tokens
  #{:ATMChain :Centra :ROL})

(def removed-fiat-currencies
  #{:bmd :bzd :gmd :gyd :kyd :lak :lrd :ltl :mkd :mnt :nio :sos :srd :yer})

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn migration [old-realm new-realm]
  (log/debug "migrating accounts schema v4")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (update-in [:wallet :visible-tokens :mainnet]
                                        #(set/difference % removed-tokens))
                             (update-in [:wallet :currency]
                                        #(if (removed-fiat-currencies %) :usd %)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))