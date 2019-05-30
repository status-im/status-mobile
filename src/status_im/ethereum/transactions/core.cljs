(ns status-im.ethereum.transactions.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.transactions.etherscan :as transactions.etherscan]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]))

(def confirmations-count-threshold 12)

(defn add-padding [address]
  {:pre [(string? address)]}
  (str "0x000000000000000000000000" (subs address 2)))

(defn- remove-padding [address]
  {:pre [(string? address)]}
  (str "0x" (subs address 26)))

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
               :value         (money/bignumber data)
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
  (json-rpc/call
   {:method "eth_getTransactionReceipt"
    :params [hash]
    :on-success
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
                    :value  (str (decode/uint value))}))])))}))

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
        (json-rpc/call
         {:method "eth_getBlockByHash"
          :params [block-hash false]
          :on-success
          (fn [{:keys [timestamp number]}]
            (let [timestamp (str (* timestamp 1000))]
              (doseq [{:keys [hash] :as transfer} block-transfers]
                (json-rpc/call
                 {:method "eth_getTransactionByHash"
                  :params [hash]
                  :on-success
                  (fn [{:keys [gasPrice gas input nonce]}]
                    (json-rpc/call
                     {:method "eth_getTransactionReceipt"
                      :params [hash]
                      :on-success
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
                                     :nonce     (str (decode/uint nonce))))]))}))}))))})))))

;; -----------------------------------------------
;; transactions api
;; -----------------------------------------------

(fx/defn watch-transaction
  "Set a watch for the given transaction
   `watch-params` needs to contain a `trigger-fn` and `on-trigger` functions
   `trigger-fn` is a function that returns true if the watch has been triggered
   `on-trigger` is a function that returns the effects to apply when the
   transaction has been triggered"
  [{:keys [db]} transaction-id {:keys [trigger-fn on-trigger] :as watch-params}]
  (when (and (fn? trigger-fn)
             (fn? on-trigger))
    {:db (assoc-in db [:ethereum/watched-transactions transaction-id]
                   watch-params)}))

(fx/defn check-transaction
  "Check if the transaction has been triggered and applies the effects returned
   by `on-trigger` if that is the case"
  [{:keys [db] :as cofx} {:keys [hash] :as transaction}]
  (when-let [watch-params
             (get-in db [:ethereum/watched-transactions hash])]
    (let [{:keys [trigger-fn on-trigger]} watch-params]
      (when (trigger-fn db transaction)
        (fx/merge cofx
                  {:db (update db :ethereum/watched-transactions
                               dissoc hash)}
                  (on-trigger transaction))))))

(fx/defn check-watched-transactions
  [{:keys [db] :as cofx}]
  (let [watched-transactions
        (select-keys (get-in db [:wallet :transactions])
                     (keys (get db :ethereum/watched-transactions)))]
    (apply fx/merge cofx
           (map (fn [[_ transaction]]
                  (check-transaction transaction))
                watched-transactions))))

(fx/defn new
  [{:keys [db] :as cofx} {:keys [hash] :as transaction}]
  (fx/merge cofx
            {:db (assoc-in db [:wallet :transactions hash] transaction)}
            (check-transaction transaction)
            wallet/update-balances))

(fx/defn handle-history
  [{:keys [db] :as cofx} transactions]
  (fx/merge cofx
            {:db (update-in db
                            [:wallet :transactions]
                            #(merge transactions %))}
            wallet/update-balances))

(fx/defn handle-token-history
  [{:keys [db]} transactions]
  {:db (update-in db
                  [:wallet :transactions]
                  merge transactions)})

(fx/defn initialize
  [cofx]
  (transactions.etherscan/fetch-history cofx))
