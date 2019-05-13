(ns status-im.ethereum.transactions.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.transactions.etherscan :as transactions.etherscan]
            [status-im.native-module.core :as status]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def confirmations-count-threshold 12)

(defn get-block-by-hash
  [block-hash callback]
  (status/call-private-rpc
   (types/clj->json {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_getBlockByHash"
                     :params  [block-hash true]})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result
                     (update :number decode/uint)
                     (update :timestamp decode/uint)))))))

(defn get-transaction-by-hash
  [transaction-hash callback]
  (status/call-private-rpc
   (types/clj->json {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_getTransactionByHash"
                     :params  [transaction-hash]})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result))))))

(defn get-transaction-receipt [transaction-hash callback]
  (status/call-private-rpc
   (types/clj->json {:jsonrpc "2.0"
                     :id      1
                     :method  "eth_getTransactionReceipt"
                     :params  [transaction-hash]})
   (fn [response]
     (if (string/blank? response)
       (log/warn :web3-response-error)
       (callback (-> (.parse js/JSON response)
                     (js->clj :keywordize-keys true)
                     :result))))))

(defn add-padding [address]
  {:pre [(string? address)]}
  (str "0x000000000000000000000000" (subs address 2)))

(defn- remove-padding [topic]
  {:pre [(string? topic)]}
  (str "0x" (subs topic 26)))

(def default-erc20-token
  {:symbol   :ERC20
   :decimals 18
   :name     "ERC20"})

(defn- parse-token-transfer
  [chain-tokens direction transfer]
  (let [{:keys [blockHash transactionHash topics data address]} transfer
        [_ from to] topics
        {:keys [nft? symbol] :as token}  (get chain-tokens address
                                              default-erc20-token)]
    (when-not nft?
      (cond-> {:hash          transactionHash
               :symbol        symbol
               :from          (remove-padding from)
               :to            (remove-padding to)
               :value         (ethereum/hex->bignumber data)
               :type          direction
               :token         token
               :error?        false
               ;; NOTE(goranjovic) - just a flag we need when we merge this entry
               ;; with the existing entry in the app, e.g. transaction info with
               ;; gas details, or a previous transfer entry with old confirmations
               ;; count.
               :transfer      true}
        (= :inbound direction)
        (assoc :block-hash blockHash)))))

(defn enrich-transaction-from-new-block
  [chain-tokens
   {:keys [number timestamp]}
   {:keys [transfer direction hash gasPrice value gas from input nonce to] :as transaction}]
  (get-transaction-receipt
   hash
   (fn [{:keys [gasUsed logs] :as receipt}]
     (let [[event _ _] (:topics (first logs))
           transfer    (= constants/event-transfer-hash event)]
       (re-frame/dispatch
        [:ethereum.transactions/new
         (merge {:block     (str number)
                 :timestamp (str (* timestamp 1000))
                 :gas-used  (str (decode/uint gasUsed))
                 :gas-price (str (decode/uint gasPrice))
                 :gas-limit (str (decode/uint gas))
                 :nonce     (str (decode/uint nonce))
                 :data      input}
                (if transfer
                  (parse-token-transfer chain-tokens
                                        :outbound
                                        (first logs))
                  ;; this is not a ERC20 token transaction
                  {:hash   hash
                   :symbol :ETH
                   :from   from
                   :to     to
                   :type   direction
                   :value  (str (decode/uint value))}))])))))

(re-frame/reg-fx
 :ethereum.transactions/enrich-transactions-from-new-blocks
 (fn [{:keys [chain-tokens block transactions]}]
   (doseq [transaction transactions]
     (enrich-transaction-from-new-block chain-tokens
                                        block
                                        transaction))))

(defn inbound-token-transfer-handler
  "The handler gets a list of inbound token transfer events and parses each
   transfer. Transfers are grouped by block the following chain of callbacks
   follows:
   - get block by hash is called to get the `timestamp` of each block
   - get transaction by hash is called on each transaction to get the `gasPrice`
   `gas` used, `input` data and `nonce` of each transaction
   - get transaction receipt is used to get the `gasUsed`
   - finally everything is merged into one map that is dispatched in a
   `ethereum.signal/new-transaction` event for each transfer"
  [chain-tokens]
  (fn [transfers]
    (let [transfers-by-block
          (group-by :block-hash
                    (keep #(parse-token-transfer
                            chain-tokens
                            :inbound
                            %)
                          transfers))]
      ;; TODO: remove this callback chain by implementing a better status-go api
      ;; This function takes the map of supported tokens as params and returns a
      ;; handler for token transfer events
      (doseq [[block-hash block-transfers] transfers-by-block]
        (get-block-by-hash
         block-hash
         (fn [{:keys [timestamp number]}]
           (let [timestamp (str (* timestamp 1000))]
             (doseq [{:keys [hash] :as transfer} block-transfers]
               (get-transaction-by-hash
                hash
                (fn [{:keys [gasPrice gas input nonce]}]
                  (get-transaction-receipt
                   hash
                   (fn [{:keys [gasUsed]}]
                     (re-frame/dispatch
                      [:ethereum.transactions/new
                       (-> transfer
                           (dissoc :block-hash)
                           (assoc :timestamp timestamp
                                  :block     (str number)
                                  :gas-used  (str (decode/uint gasUsed))
                                  :gas-price (str (decode/uint gasPrice))
                                  :gas-limit (str (decode/uint gas))
                                  :data      input
                                  :nonce     (str (decode/uint nonce))))])))))))))))))

;; -----------------------------------------------
;; transactions api
;; -----------------------------------------------

(fx/defn new
  [{:keys [db]} {:keys [hash] :as transaction}]
  {:db (assoc-in db [:wallet :transactions hash] transaction)})

(fx/defn handle-history
  [{:keys [db]} transactions]
  {:db (update-in db
                  [:wallet :transactions]
                  #(merge transactions %))})

(fx/defn handle-token-history
  [{:keys [db]} transactions]
  {:db (update-in db
                  [:wallet :transactions]
                  merge transactions)})

(fx/defn initialize
  [cofx]
  (transactions.etherscan/fetch-history cofx))
