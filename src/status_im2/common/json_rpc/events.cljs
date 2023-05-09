(ns status-im2.common.json-rpc.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [react-native.background-timer :as background-timer]
            [native-module.core :as native-module]
            [taoensso.timbre :as log]
            [utils.transforms :as transforms]))

(defn on-error-retry
  [call-method {:keys [method number-of-retries delay on-error] :as arg}]
  (if (pos? number-of-retries)
    (fn [error]
      (let [updated-delay (if delay
                            (min 2000 (* 2 delay))
                            50)]
        (log/debug "[on-error-retry]"  method
                   "number-of-retries" number-of-retries
                   "delay"             delay
                   "error"             error)
        (background-timer/set-timeout #(call-method (-> arg
                                                        (update :number-of-retries dec)
                                                        (assoc :delay updated-delay)))
                                      updated-delay)))
    on-error))

(defn call
  [{:keys [method params on-success on-error js-response] :as arg}]
  (let [params   (or params [])
        on-error (or on-error
                     (on-error-retry call arg)
                     #(log/warn :json-rpc/error method :error % :params params))]
    (native-module/call-private-rpc
     (transforms/clj->json {:jsonrpc "2.0"
                            :id      1
                            :method  method
                            :params  params})
     (fn [response]
       (if (string/blank? response)
         (on-error {:message "Blank response"})
         (let [response-js (transforms/json->js response)]
           (if (.-error response-js)
             (on-error (transforms/js->clj (.-error response-js)))
             (on-success (if js-response
                           (.-result response-js)
                           (transforms/js->clj (.-result response-js)))))))))))

(re-frame/reg-fx
 :json-rpc/call
 (fn [params]
   (doseq [param params]
     (call param))))
