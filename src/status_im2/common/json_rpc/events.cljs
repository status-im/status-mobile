(ns status-im2.common.json-rpc.events
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [re-frame.core :as re-frame]
            [react-native.background-timer :as background-timer]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defn- on-error-retry
  [call-method
   {method            :method
    number-of-retries :number-of-retries
    delay-ms          :delay
    on-error          :on-error
    :as               arg}]
  (if (pos? number-of-retries)
    (fn [error]
      (let [updated-delay (if delay-ms
                            (min 2000 (* 2 delay-ms))
                            50)]
        (log/debug "[on-error-retry]"  method
                   "number-of-retries" number-of-retries
                   "delay"             delay-ms
                   "error"             error)
        (background-timer/set-timeout #(call-method (-> arg
                                                        (update :number-of-retries dec)
                                                        (assoc :delay updated-delay)))
                                      updated-delay)))
    on-error))

(defn call
  "Call private RPC endpoint.

  method: string - The name of an endpoint function in status-go, with the first
  character lowercased and prefixed by wakuext_. For example, the BackupData
  function should be represented as the string wakuext_backupData.

  params: sequence - A positional sequence of zero or more arguments.

  on-success/on-error: function/vector (optional) - When a function, it will be
  called with the transformed response as the sole argument. When a vector, it
  is expected to be a valid re-frame event vector, and the event will be
  dispatched with the transformed response conj'ed at the end.

  js-response: boolean - When non-nil, the successful response will not be
  recursively converted to Clojure data structures. Default: nil.

  number-of-retries: integer - The maximum number of retries in case of failure.
  Default: nil.

  delay: integer - The number of milliseconds to wait between retries. Default:
  nil.

  Note that on-error is optional, but if not provided, a default implementation
  will be used.
  "
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
     (fn [raw-response]
       (if (string/blank? raw-response)
         (let [error {:message "Blank response"}]
           (if (vector? on-error)
             (rf/dispatch (conj on-error error))
             (on-error error)))
         (let [^js response-js (transforms/json->js raw-response)]
           (if-let [error (.-error response-js)]
             (let [error (transforms/js->clj error)]
               (if (vector? on-error)
                 (rf/dispatch (conj on-error error))
                 (on-error error)))
             (when on-success
               (let [result (if js-response
                              (.-result response-js)
                              (transforms/js->clj (.-result response-js)))]
                 (if (vector? on-success)
                   (rf/dispatch (conj on-success result))
                   (on-success result)))))))))))

(re-frame/reg-fx
 :json-rpc/call
 (fn [params]
   (doseq [param params]
     (call param))))
