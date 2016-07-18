(ns status-im.persistence.simple-kv-store
  (:require [status-im.protocol.state.storage :as st]
            [status-im.persistence.realm.core :as r]
            [status-im.utils.types :refer [to-edn-string]]))

(defrecord SimpleKvStore [schema]
  st/Storage
  (put [_ key value]
    (r/write schema
      (fn []
        (r/create schema :kv-store 
                  {:key   key
                   :value (to-edn-string value)} true))))
  (get [_ key]
    (some-> (r/get-by-field schema :kv-store :key key)
            (r/single-cljs)
            (r/decode-value)))
  (contains-key? [_ key]
    (r/exists? schema :kv-store :key key))
  (delete [_ key]
    (r/write schema
             (fn []
               (->> (r/get-by-field schema :kv-store :key key)
                    (r/single)
                    (r/delete schema))))))

(def kv-store (->SimpleKvStore :account))

(def base-kv-store (->SimpleKvStore :base))
