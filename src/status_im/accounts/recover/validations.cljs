(ns status-im.accounts.recover.validations
  (:require [cljs.spec :as s]
            [cljsjs.web3]))

(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::passphrase ::not-empty-string)
(s/def ::password ::not-empty-string)
