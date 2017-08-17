(ns status-im.ui.screens.accounts.recover.db
  (:require [cljs.spec.alpha :as s]
            status-im.utils.db))

(s/def ::passphrase :global/not-empty-string)
(s/def ::password :global/not-empty-string)