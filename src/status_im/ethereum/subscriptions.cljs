(ns status-im.ethereum.subscriptions
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.decode :as decode]
            [status-im.native-module.core :as status]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

;; NOTE: this is the safe block range that can be
;; queried from infura rpc gateway without getting timeouts
;; determined experimentally by @goranjovic
(def block-query-limit 100000)

(defn get-latest-block [callback]
  (status/call-private-rpc
   (types/json->clj {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_blockNumber"
                     :params  []})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result
                     decode/uint))))))

(defn get-block-by-hash [block-hash callback]
  (status/call-private-rpc
   (types/json->clj {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_getBlockByHash"
                     :params  [block-hash false]})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result
                     (update :number decode/uint)
                     (update :timestamp decode/uint)))))))

(defn- get-token-transfer-logs
  [from-block {:keys [chain-tokens direction from to]} callback]
  (status/call-private-rpc
   (types/json->clj {:jsonrpc "2.0"
                     :id      2
                     :method  "eth_getLogs"
                     :params
                     [{:address (keys chain-tokens)
                       :fromBlock from-block
                       :topics    [constants/event-transfer-hash from to]}]})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result))))))

(fx/defn handle-signal
  [cofx {:keys [subscription_id data] :as event}]
  (if-let [handler (get-in cofx [:db :ethereum/subscriptions subscription_id])]
    (handler data)
    (log/warn ::unknown-subscription :event event)))

(fx/defn handle-error
  [cofx {:keys [subscription_id data] :as event}]
  (log/error ::error event))

(fx/defn register-subscription
  [{:keys [db]} id handler]
  {:db (assoc-in db [:ethereum/subscriptions id] handler)})

(fx/defn new-block
  [{:keys [db]} block-number]
  {:db (assoc-in db [:ethereum/current-block] block-number)})

(defn subscribe-signal
  [filter params callback]
  (status/call-private-rpc
   (types/clj->json {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_subscribeSignal"
                     :params  [filter params]})
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

(defn- add-padding [address]
  {:pre [(string? address)]}
  (str "0x000000000000000000000000" (subs address 2)))

(defn- remove-padding [topic]
  {:pre [(string? topic)]}
  (str "0x" (subs topic 26)))

(defn- parse-transaction-entries [timestamp chain-tokens direction transfers]
  {:pre [(integer? timestamp)
         (map? chain-tokens)
         (every? (fn [[k v]] (and (string? k) (map? v))) chain-tokens)
         (keyword? direction)
         (every? map? transfers)]}
  (into {}
        (keep identity
              (for [transfer transfers]
                (when-let [token (->> transfer :address (get chain-tokens))]
                  (when-not (:nft? token)
                    [(:transactionHash transfer)
                     {:block         (str (-> transfer :blockNumber ethereum/hex->bignumber))
                      :hash          (:transactionHash transfer)
                      :symbol        (:symbol token)
                      :from          (some-> transfer :topics second remove-padding)
                      :to            (some-> transfer :topics last remove-padding)
                      :value         (-> transfer :data ethereum/hex->bignumber)
                      :type          direction
                      :gas-price     nil
                      :nonce         nil
                      :data          nil
                      :gas-limit     nil
                      :timestamp     (str (* timestamp 1000))
                      :gas-used      nil
                      ;; NOTE(goranjovic) - metadata on the type of token: contains name, symbol, decimas, address.
                      :token         token
                      ;; NOTE(goranjovic) - if an event has been emitted, we can say there was no error
                      :error?        false
                      ;; NOTE(goranjovic) - just a flag we need when we merge this entry with the existing entry in
                      ;; the app, e.g. transaction info with gas details, or a previous transfer entry with old
                      ;; confirmations count.
                      :transfer      true}]))))))

(letfn [(combine-entries [transaction token-transfer]
          (merge transaction (select-keys token-transfer [:symbol :from :to :value :type :token :transfer])))
        (tx-and-transfer? [tx1 tx2]
                          (and (not (:transfer tx1)) (:transfer tx2)))
        (both-transfer?
         [tx1 tx2]
         (and (:transfer tx1) (:transfer tx2)))]
  (defn- dedupe-transactions [tx1 tx2]
    (cond (tx-and-transfer? tx1 tx2) (combine-entries tx1 tx2)
          (tx-and-transfer? tx2 tx1) (combine-entries tx2 tx1)
          :else tx2)))

(fx/defn new-transactions
  [{:keys [db]} transactions]
  {:db (update-in db
                  [:wallet :transactions]
                  #(merge-with dedupe-transactions % transactions))})

(defn transactions-handler
  [{:keys [chain-tokens from to direction]}]
  (fn [transfers]
    (let [transfers-by-block (group-by :blockHash transfers)]
      (doseq [[block-hash block-transfers] transfers-by-block]
        (get-block-by-hash
         block-hash
         (fn [{:keys [timestamp]}]
           (let [transactions (parse-transaction-entries timestamp
                                                         chain-tokens
                                                         direction
                                                         block-transfers)]
             (when (not-empty transactions)
               (re-frame/dispatch [:ethereum.signal/new-transactions
                                   transactions])))))))))

;; Here we are querying event logs for Transfer events.
;;
;; The parameters are as follows:
;; - address - token smart contract address
;; - fromBlock - we need to specify it, since default is latest
;; - topics[0] - hash code of the Transfer event signature
;; - topics[1] - address of token sender with leading zeroes padding up to 32 bytes
;; - topics[2] - address of token sender with leading zeroes padding up to 32 bytes
(defn new-token-transaction-filter
  [{:keys [chain-tokens from to] :as args}]
  (subscribe-signal
   "eth_newFilter"
   [{:fromBlock "latest"
     :toBlock "latest"
     :address (keys chain-tokens)
     :topics [constants/event-transfer-hash from to]}]
   (transactions-handler args)))

(defn new-block-filter
  []
  (subscribe-signal
   "eth_newBlockFilter" []
   (fn [[block-hash]]
     (get-block-by-hash
      block-hash
      (fn [block]
        (when-let [block-number (:number block)]
          (re-frame/dispatch [:ethereum.signal/new-block
                              block-number])))))))

(defn get-from-block
  [current-block-number]
  (-> current-block-number
      (- block-query-limit)
      (max 0)
      ethereum/int->hex))

(re-frame/reg-fx
 :ethereum.subscriptions/token-transactions
 (fn [{:keys [address] :as args}]
   (let [inbound-args  (merge args
                              {:direction :inbound
                               :to address})
         outbound-args (merge args
                              {:direction :outbound
                               :from address})]
     ;; fetch 2 weeks of history until transactions are persisted
     (get-latest-block
      (fn [current-block-number]
        (let [from-block (get-from-block current-block-number)]
          (get-token-transfer-logs from-block inbound-args
                                   (transactions-handler inbound-args))
          (get-token-transfer-logs from-block outbound-args
                                   (transactions-handler outbound-args)))))
     ;; start inbound and outbound token transaction subscriptions
     (new-token-transaction-filter inbound-args)
     (new-token-transaction-filter outbound-args))))

(re-frame/reg-fx
 :ethereum.subscriptions/new-block
 new-block-filter)

(fx/defn initialize
  [{:keys [db] :as cofx}]
  (let [{:keys [:account/account :wallet/all-tokens network]} db
        chain (ethereum/network->chain-keyword (get-in account [:networks network]))
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))
        padded-address (add-padding (ethereum/normalized-address (:address account)))]
    {:ethereum.subscriptions/new-block nil
     :ethereum.subscriptions/token-transactions {:chain-tokens chain-tokens
                                                 :address padded-address}}))
