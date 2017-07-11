(ns status-im.transactions.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :transactions/transactions (s/nilable map?))                                 ;; {id (string) transaction (map)}
(s/def :transactions/transactions-queue (s/nilable map?))                           ;; {id (string) transaction (map)}
(s/def :transactions/selected-transaction (s/nilable map?))
(s/def :transactions/confirm-transactions (s/nilable map?))
(s/def :transactions/confirmed-transactions-count (s/nilable int?))
(s/def :transactions/transactions-list-ui-props (s/nilable map?))
(s/def :transactions/transaction-details-ui-props (s/nilable map?))
(s/def :transactions/wrong-password-counter (s/nilable int?))
(s/def :transactions/wrong-password? (s/nilable boolean?))