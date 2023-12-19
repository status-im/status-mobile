(ns legacy.status-im.multiaccounts.db
  (:require
    [cljs.spec.alpha :as spec]
    [status-im2.constants :as const]))

(defn valid-length?
  [password]
  (>= (count password) const/min-password-length))

(spec/def ::password (spec/and string? not-empty valid-length?))
