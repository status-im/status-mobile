(ns tests.contract-test.utils
  (:require
    [status-im.common.json-rpc.events :as rpc-events]
    [utils.number]))

(defn call-rpc-endpoint
  [{:keys [rpc-endpoint
           params
           action
           on-error]}]
  (rpc-events/call {:method     rpc-endpoint
                    :params     params
                    :on-success action
                    :on-error   on-error}))
