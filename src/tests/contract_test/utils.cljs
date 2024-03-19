(ns tests.contract-test.utils
  (:require
    [promesa.core :as p]
    [status-im.common.json-rpc.events :as rpc-events]
    [utils.number]))

(defn call-rpc
  [method & args]
  (p/create
   (fn [p-resolve p-reject]
     (rpc-events/call {:method     method
                       :params     args
                       :on-success p-resolve
                       :on-error   p-reject}))))

(defn get-main-account
  [accounts]
  (:address (first accounts)))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))
