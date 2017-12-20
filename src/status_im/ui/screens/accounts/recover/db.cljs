(ns status-im.ui.screens.accounts.recover.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :as const]
            status-im.utils.db))

(defn valid-length? [password]
  (>= (count password) const/min-password-length))

(spec/def ::passphrase :global/not-empty-string)
(spec/def ::password  (spec/and :global/not-empty-string valid-length?))
