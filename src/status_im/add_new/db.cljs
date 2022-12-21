(ns status-im.add-new.db
  (:require [status-im.ethereum.ens :as ens]
            [cljs.spec.alpha :as spec]
            [status-im.utils.db :as utils.db]))

(defn own-public-key?
  [{:keys [multiaccount]} public-key]
  (= (:public-key multiaccount) public-key))

(defn validate-pub-key [db public-key]
  (cond
    (or (not (utils.db/valid-public-key? public-key))
        (= public-key ens/default-key))
    :invalid
    (own-public-key? db public-key)
    :yourself))

(spec/def ::name (spec/and string? not-empty))

(spec/def ::topic (spec/and string?
                            not-empty
                            (partial re-matches #"[a-z0-9\-]+")))

(defn valid-topic? [topic]
  (and topic
       (spec/valid? ::topic topic)
       (not (utils.db/valid-public-key? topic))))