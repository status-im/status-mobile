(ns status-im.utils.db
  (:require [cljs.spec.alpha :as spec]))

(defn valid-public-key? [s]
  (boolean (re-matches #"0x04[0-9a-f]{128}" s)))

(spec/def :global/not-empty-string (spec/and string? not-empty))
(spec/def :global/public-key (spec/and :global/not-empty-string valid-public-key?))

