(ns tests.contract-test.utils
  (:require
    [utils.number]))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))

(defn get-default-address
  [accounts]
  (:address (get-default-account accounts)))
