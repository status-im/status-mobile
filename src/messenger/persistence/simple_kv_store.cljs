(ns messenger.persistence.simple-kv-store
  (:require [syng-im.protocol.state.storage :as st]
            [messenger.persistence.realm :as r]))

(defrecord SimpleKvStore []
  st/Storage
  (put [_ key value]
    (r/write
      (fn []
        (r/create :kv-store {:key   key
                             :value (with-out-str (pr value))} true))))
  (get [_ key]
    (some-> (r/get-by-field :kv-store :key key)
            (r/single-cljs)
            (r/decode-value)))
  (contains-key? [_ key]
    (r/exists? :kv-store :key key))
  (delete [_ key]
    (-> (r/get-by-field :kv-store :key key)
        (r/single)
        (r/delete))))

(comment

  )