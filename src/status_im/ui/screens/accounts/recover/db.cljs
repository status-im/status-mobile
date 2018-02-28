(ns status-im.ui.screens.accounts.recover.db
  (:require [cljs.spec.alpha :as spec]))

(spec/def ::passphrase :global/not-empty-string)
