(ns status-im.ethereum.transactions.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.encode :as encode]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]))

(def confirmations-count-threshold 12)

(def etherscan-supported? #{:testnet :mainnet :rinkeby})

(let [network->subdomain {:testnet "ropsten" :rinkeby "rinkeby"}]
  (defn get-transaction-details-url [chain hash]
    {:pre [(keyword? chain) (string? hash)]
     :post [(or (nil? %) (string? %))]}
    (when (etherscan-supported? chain)
      (let [network-subdomain (when-let [subdomain (network->subdomain chain)]
                                (str subdomain "."))]
        (str "https://" network-subdomain "etherscan.io/tx/" hash)))))

(def default-erc20-token
  {:symbol   :ERC20
   :decimals 18
   :name     "ERC20"})

(defn direction
  [address to]
  (if (= address to)
    :inbound
    :outbound))

(defn- parse-token-transfer
  [chain-tokens contract]
  (let [{:keys [nft? symbol] :as token}  (get chain-tokens contract
                                              default-erc20-token)]
    {:symbol        symbol
     :token         token
     ;; NOTE(goranjovic) - just a flag we need when we merge this entry
     ;; with the existing entry in the app, e.g. transaction info with
     ;; gas details, or a previous transfer entry with old confirmations
     ;; count.
     :transfer      true}))

(defn enrich-transfer
  [chain-tokens
   {:keys [address blockNumber timestamp type transaction receipt from txStatus
           txHash gasPrice gasUsed contract value gasLimit input nonce to type id] :as transfer}]
  (let [erc20?  (= type "erc20")
        failed? (= txStatus "0x0")]
    (merge {:id        id
            :block     (str (decode/uint blockNumber))
            :timestamp (* (decode/uint timestamp) 1000)
            :gas-used  (str (decode/uint gasUsed))
            :gas-price (str (decode/uint gasPrice))
            :gas-limit (str (decode/uint gasLimit))
            :nonce     (str (decode/uint nonce))
            :hash      txHash
            :data      input
            :from      from
            :to        to
            :type      (if failed?
                         :failed
                         (direction address to))
            :value     (str (decode/uint value))}
           (if erc20?
             (parse-token-transfer chain-tokens contract)
             ;; this is not a ERC20 token transaction
             {:symbol :ETH}))))

(defn enrich-transfers
  [chain-tokens transfers]
  (mapv (fn [transfer]
          (enrich-transfer chain-tokens transfer))
        transfers))

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
    (apply fx/merge
           cofx
           (map (fn [[_ transaction]]
                  (check-transaction transaction))
                watched-transactions))))

(fx/defn add-transfer
  [{:keys [db] :as cofx} {:keys [hash id] :as transfer} address]
  (let [transfer-by-hash (get-in db [:wallet :accounts address :transactions hash])]
        ;;transfer-by-id   (get-in db [:wallet :transaction id]) ;; TODO didn't found any usage of this
    (when-let [unique-id (when (not= transfer transfer-by-hash) ;(or transfer-by-id)
                           (if (and transfer-by-hash
                                    (not (= :pending
                                            (:type transfer-by-hash))))
                             id
                             hash))]
      (fx/merge cofx
                {:db (assoc-in db [:wallet :accounts address :transactions unique-id]
                               (assoc transfer :hash unique-id))}
                (check-transaction transfer)))))

(fx/defn new-transfers
  {:events [::new-transfers]}
  [{:keys [db] :as cofx} address transfers]
  (let [add-transfers-fx (map #(add-transfer % address) transfers)]
    (apply fx/merge cofx (conj add-transfers-fx
                               (wallet/update-balances [address])))))

(fx/defn handle-token-history
  [{:keys [db]} transactions]
  {:db (update-in db
                  [:wallet :transactions]
                  merge transactions)})

(re-frame/reg-fx
 ::get-transfers
 (fn [{:keys [accounts chain-tokens from-block to-block]
       :or {from-block "0"
            to-block nil}}]
   ;; start inbound token transaction subscriptions
   ;; outbound token transactions are already caught in new blocks filter
   (doseq [{:keys [address]} accounts]
     (json-rpc/call
      {:method "wallet_getTransfersByAddress"
       :params [address (encode/uint from-block) (encode/uint to-block)]
       :on-success #(re-frame/dispatch [::new-transfers address (enrich-transfers chain-tokens %)])}))))

(fx/defn initialize
  [{:keys [db] :as cofx}]
  (let [accounts (get-in db [:multiaccount :accounts]) ;;TODO https://github.com/status-im/status-go/issues/1566
        {:keys [:wallet/all-tokens]} db
        chain (ethereum/chain-keyword db)
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))]
    {::get-transfers {:accounts accounts :chain-tokens chain-tokens}}))
