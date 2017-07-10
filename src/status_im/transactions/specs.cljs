(ns status-im.transactions.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :transactions/transactions map?)                                 ;; {id (string) transaction (map)}
(s/def :transactions/transactions-queue map?)                           ;; {id (string) transaction (map)}
(s/def :transactions/selected-transaction map?)
(s/def :transactions/confirm-transactions map?)
(s/def :transactions/confirmed-transactions-count int?)
(s/def :transactions/transactions-list-ui-props map?)
(s/def :transactions/transaction-details-ui-props map?)
(s/def :transactions/wrong-password-counter int?)
(s/def :transactions/wrong-password? boolean?)