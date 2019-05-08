(ns status-im.ethereum.subscriptions
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.decode :as decode]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn get-block-by-hash [block-hash callback]
  (status/call-private-rpc
   (.stringify js/JSON (clj->js {:jsonrpc "2.0"
                                 :id      1
                                 :method  "eth_getBlockByHash"
                                 :params  [block-hash false]}))
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result
                     :number
                     decode/uint))))))

(fx/defn handle-signal
  [cofx {:keys [subscription_id data] :as event}]
  (if-let [handler (get-in cofx [:db :ethereum/subscriptions subscription_id])]
    (handler data)
    (log/warn ::unknown-subscription :event event)))

(fx/defn register-subscription
  [{:keys [db]} id handler]
  {:db (assoc-in db [:ethereum/subscriptions id] handler)})

(fx/defn new-block
  [{:keys [db]} block-number]
  {:db (assoc-in db [:ethereum/current-block] block-number)})

(defn subscribe-signal
  [filter params callback]
  (status/call-private-rpc
   (.stringify js/JSON (clj->js {:jsonrpc "2.0"
                                 :id      1
                                 :method  "eth_subscribeSignal"
                                 :params  [filter, params]}))
   (fn [response]
     (if (string/blank? response)
       (log/error ::subscription-unknown-error :filter filter :params params)
       (let [{:keys [error result]}
             (-> (.parse js/JSON response)
                 (js->clj :keywordize-keys true))]
         (if error
           (log/error ::subscription-error error :filter filter :params params)
           (re-frame/dispatch [:ethereum.callback/subscription-success
                               result
                               callback])))))))

(defn new-block-filter
  []
  (subscribe-signal
   "eth_newBlockFilter" []
   (fn [[block-hash]]
     (get-block-by-hash
      block-hash
      (fn [block-number]
        (when block-number
          (re-frame/dispatch [:ethereum.signal/new-block
                              block-number])))))))

(re-frame/reg-fx
 :ethereum.subscriptions/new-block-filter
 new-block-filter)

(fx/defn initialize
  [cofx]
  {:ethereum.subscriptions/new-block-filter nil})
