(ns status-im.multiaccounts.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :as const]))

(defn valid-length? [password]
  (>= (count password) const/min-sign-in-password-length))

(spec/def ::password  (spec/and :global/not-empty-string valid-length?))
