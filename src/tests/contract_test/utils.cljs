(ns tests.contract-test.utils
  (:require
    [promesa.core :as promesa]
    [status-im.common.json-rpc.events :as rpc-events]
    [utils.number]))

(defn call-rpc
  [method & args]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc-events/call {:method     method
                       :params     args
                       :on-success p-resolve
                       :on-error   p-reject}))))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))

(defn get-default-address
  [accounts]
  (:address (get-default-account accounts)))
