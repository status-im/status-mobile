(ns status-im.accounts.recover.validations
  (:require [cljs.spec :as s]
            [cljsjs.web3]
            [status-im.persistence.realm.core :as realm]))

(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::passphrase ::not-empty-string)
(s/def ::password ::not-empty-string)
