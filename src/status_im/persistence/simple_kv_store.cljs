(ns status-im.persistence.simple-kv-store
  (:require [status-im.protocol.state.storage :as st]
            [status-im.persistence.realm :as r]
            [status-im.utils.types :refer [to-edn-string]]))

(defrecord SimpleKvStore []
  st/Storage
  (put [_ key value]
    (r/write
      (fn []
        (r/create :kv-store {:key   key
                             :value (to-edn-string value)} true))))
  (get [_ key]
    (some-> (r/get-by-field :kv-store :key key)
            (r/single-cljs)
            (r/decode-value)))
  (contains-key? [_ key]
    (r/exists? :kv-store :key key))
  (delete [_ key]
    (r/write (fn []
               (-> (r/get-by-field :kv-store :key key)
                   (r/single)
                   (r/delete))))))

(def kv-store (->SimpleKvStore))
