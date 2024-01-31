(ns status-im.tests.contract-test.utils
  (:require
   [status-im.common.json-rpc.events :as rpc-events]
   [utils.number]))

(defn check-response [validator response callback]
  (let [test-checker (js/Promise.
                      (fn [res reject]
                        (if (validator response)
                          (res "test passed")
                          (reject "test failed"))))]
    (.then test-checker
           (fn [value] (callback value))
           (fn [reason] (println "Test failed due to:" reason)))))

(defn call-rpc-endpoint
  [{:keys [rpc-endpoint
           params
           check-result]}
   callback]
  (rpc-events/call {:method     rpc-endpoint
                    :params     params
                    :on-success #(check-response check-result % callback)
                    :on-error   #(prn {:title (str "failed test " rpc-endpoint)
                                       :error %})}))
