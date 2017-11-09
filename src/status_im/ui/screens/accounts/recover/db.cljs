(ns status-im.ui.screens.accounts.recover.db
  (:require [cljs.spec.alpha :as spec]
            status-im.utils.db))

(defn valid-length? [identity]
  (> (count identity) 5))

(spec/def ::passphrase :global/not-empty-string)
(spec/def ::password  (spec/and :global/not-empty-string valid-length?))
