(ns status-im.ethereum.json-rpc
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.native-module.core :as status]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(defn on-error-retry
  [call-method {:keys [method number-of-retries delay on-error] :as arg}]
  (if (pos? number-of-retries)
    (fn [error]
      (let [updated-delay (if delay
                            (min 2000 (* 2 delay))
                            50)]
        (log/debug "[on-error-retry]" method "number-of-retries" number-of-retries "delay" delay "error" error)
        (utils/set-timeout #(call-method (-> arg
                                             (update :number-of-retries dec)
                                             (assoc :delay updated-delay)))
                           updated-delay)))
    on-error))

(defn call
  [{:keys [method params on-success on-error js-response] :as arg}]
  (let [params (or params [])
        on-error (or on-error (on-error-retry call arg) #(log/warn :json-rpc/error method :error % :params params))]
    (status/call-private-rpc
     (types/clj->json {:jsonrpc "2.0"
                       :id      1
                       :method  method
                       :params  params})
     (fn [response]
       (if (string/blank? response)
         (on-error {:message "Blank response"})
         (let [response-js (types/json->js response)]
           (if (.-error response-js)
             (on-error (types/js->clj (.-error response-js)))
             (on-success (if js-response
                           (.-result response-js)
                           (types/js->clj (.-result response-js)))))))))))

(defn eth-call
  [{:keys [contract method params outputs on-success block]
    :or {block "latest" params []}
    :as arg}]
  (call {:method "eth_call"
         :params [{:to contract
                   :data (abi-spec/encode method params)}
                  (if (int? block)
                    (abi-spec/number-to-hex block)
                    block)]
         :on-success
         (if outputs
           #(on-success (abi-spec/decode % outputs))
           on-success)
         :on-error
         (on-error-retry eth-call arg)}))

(re-frame/reg-fx
 ::call
 (fn [params]
   (doseq [param params]
     (call param))))
