(ns status-im.add-new.db
  (:require [status-im.ethereum.ens :as ens]
            [cljs.spec.alpha :as spec]))

(defn own-public-key?
  [{:keys [multiaccount]} public-key]
  (= (:public-key multiaccount) public-key))

(defn validate-pub-key [db public-key]
  (cond
    (or (not (spec/valid? :global/public-key public-key))
        (= public-key ens/default-key))
    :invalid
    (own-public-key? db public-key)
    :yourself))

(spec/def ::name :global/not-empty-string)

(spec/def ::topic (spec/and :global/not-empty-string
                            (partial re-matches #"[a-z0-9\-]+")))

(defn valid-topic? [topic]
  (and topic
       (spec/valid? ::topic topic)
       (not (spec/valid? :global/public-key topic))))