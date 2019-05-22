(ns status-im.ethereum.subscriptions
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.tokens :as tokens]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

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

(defn keep-user-transactions
  [wallet-address transactions]
  (keep (fn [{:keys [to from] :as transaction}]
          (when-let [direction (cond
                                 (= wallet-address to)   :inbound
                                 (= wallet-address from) :outbound)]
            (assoc transaction :direction direction)))
        transactions))

(fx/defn new-block
  [{:keys [db] :as cofx} {:keys [number transactions] :as block}]
  (when number
    (let [{:keys [:wallet/all-tokens :ethereum/current-block]} db
          chain (ethereum/chain-keyword db)
          chain-tokens (into {} (map (juxt :address identity)
                                     (tokens/tokens-for all-tokens chain)))
          wallet-address (ethereum/current-address db)
          token-contracts-addresses (into #{} (keys chain-tokens))]
      (fx/merge cofx
                {:db (assoc-in db [:ethereum/current-block] number)
                 :ethereum.transactions/enrich-transactions-from-new-blocks
                 {:chain-tokens chain-tokens
                  :block block
                  :transactions (keep-user-transactions wallet-address
                                                        transactions)}}
                (when (or (not current-block)
                          (not= number (inc current-block)))
                  ;; in case we skipped some blocks or got an uncle, re-fetch history
                  ;; from etherscan
                  (transactions/initialize))))))

(defn new-token-transaction-filter
  [{:keys [chain-tokens from to] :as args}]
  (json-rpc/call
   {:method "eth_newFilter"
    :params [{:fromBlock "latest"
              :toBlock "latest"
              :topics [constants/event-transfer-hash from to]}]
    :on-success (transactions/inbound-token-transfer-handler chain-tokens)}))

(re-frame/reg-fx
 :ethereum.subscriptions/token-transactions
 (fn [{:keys [address] :as args}]
   ;; start inbound token transaction subscriptions
   ;; outbound token transactions are already caught in new blocks filter
   (new-token-transaction-filter (merge args
                                        {:direction :inbound
                                         :to address}))))

(defn new-block-filter
  []
  (json-rpc/call
   {:method "eth_newBlockFilter"
    :on-success
    (fn [[block-hash]]
      (json-rpc/call
       {:method "eth_getBlockByHash"
        :params [block-hash true]
        :on-success
        (fn [block]
          (re-frame/dispatch [:ethereum.signal/new-block block]))}))}))

(re-frame/reg-fx
 :ethereum.subscriptions/new-block
 new-block-filter)

(fx/defn initialize
  [{:keys [db] :as cofx}]
  (let [{:keys [:wallet/all-tokens]} db
        chain (ethereum/chain-keyword db)
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))
        normalized-address (ethereum/current-address db)
        padded-address (transactions/add-padding normalized-address)]
    {:ethereum.subscriptions/new-block nil
     :ethereum.subscriptions/token-transactions
     {:chain-tokens chain-tokens
      :address padded-address}}))
