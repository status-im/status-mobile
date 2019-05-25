(ns status-im.utils.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.ethereum.core :as ethereum]))

(defn valid-public-key? [s]
  (boolean (re-matches #"0x04[0-9a-f]{128}" s)))

(spec/def :global/not-empty-string (spec/and string? not-empty))
(spec/def :global/public-key (spec/and :global/not-empty-string valid-public-key?))
(spec/def :global/address ethereum/address?)

(spec/def :status/tag (spec/and :global/not-empty-string
                                (partial re-matches #"[a-z0-9\-]+")))
(spec/def :status/tags (spec/coll-of :status/tag :kind set?))
