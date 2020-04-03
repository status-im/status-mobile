(ns status-im.ethereum.transactions.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.encode :as encode]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]
            [taoensso.timbre :as log]))

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
    (merge {:address   (eip55/address->checksum address)
            :id        id
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

(def default-transfers-limit 20)

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
        (reduce-kv (fn [acc _ {:keys [transactions]}]
                     (merge acc
                            (select-keys transactions
                                         (keys (get db :ethereum/watched-transactions)))))
                   {}
                   (get-in db [:wallet :accounts]))]
    (apply fx/merge
           cofx
           (map (fn [[_ transaction]]
                  (check-transaction transaction))
                watched-transactions))))

(fx/defn add-transfer
  "We determine a unique id for the transfer before adding it because some
   transaction can contain multiple transfers and they would overwrite each other
   in the transfer map if identified by hash"
  [{:keys [db] :as cofx} {:keys [hash id address] :as transfer}]
  (let [transfer-by-hash (get-in db [:wallet :accounts address :transactions hash])]
    (when-let [unique-id (when-not (= transfer transfer-by-hash)
                           (if (and transfer-by-hash
                                    (not (= :pending
                                            (:type transfer-by-hash))))
                             id
                             hash))]
      (fx/merge cofx
                {:db (assoc-in db [:wallet :accounts address :transactions unique-id]
                               (assoc transfer :hash unique-id))}
                (check-transaction transfer)))))

(defn get-min-known-block [db address]
  (get-in db [:wallet :accounts (eip55/address->checksum address) :min-block]))

(defn min-block-transfers-count [db address]
  (get-in db [:wallet :accounts
              (eip55/address->checksum address)
              :min-block-transfers-count]))

(fx/defn set-lowest-fetched-block
  [{:keys [db]} address transfers]
  (let [checksum (eip55/address->checksum address)
        {:keys [min-block min-block-transfers-count]}
        (reduce
         (fn [{:keys [min-block] :as acc}
              {:keys [block hash]}]
           (cond
             (or (nil? min-block) (> min-block block))
             {:min-block                 block
              :min-block-transfers-count 1}

             (and (= min-block block)
                  (nil? (get-in db [:wallet :accounts checksum :transactions hash])))
             (update acc :min-block-transfers-count inc)

             :else acc))
         {:min-block
          (get-min-known-block db address)

          :min-block-transfers-count
          (min-block-transfers-count db address)}
         transfers)]
    (log/debug "[transactions] set-lowest-fetched-block"
               "address" address
               "min-block" min-block
               "min-block-transfers-count" min-block-transfers-count)
    {:db (update-in db [:wallet :accounts checksum] assoc
                    :min-block min-block
                    :min-block-transfers-count min-block-transfers-count)}))

(defn update-fetching-status
  [db addresses fetching-type state]
  (update-in
   db [:wallet :fetching]
   (fn [accounts]
     (reduce
      (fn [accounts address]
        (assoc-in accounts
                  [(eip55/address->checksum address) fetching-type]
                  state))
      accounts
      addresses))))

(fx/defn tx-fetching-in-progress
  [{:keys [db]} addresses]
  {:db (update-fetching-status db addresses :history? true)})

(fx/defn tx-fetching-ended
  [{:keys [db]} addresses]
  {:db (update-fetching-status db addresses :history? false)})

(fx/defn tx-history-end-reached
  [{:keys [db]} address]
  {:db (assoc-in db [:wallet :fetching address :all-fetched?] true)})

(fx/defn new-transfers
  {:events [::new-transfers]}
  [{:keys [db] :as cofx} transfers {:keys [address limit]}]
  (log/debug "[transfers] new-transfers"
             "address" address
             "count" (count transfers)
             "limit" limit)
  (let [checksum (eip55/address->checksum address)
        min-known-block (or (get-min-known-block db address)
                            (:ethereum/current-block db))
        effects (cond-> [(when (seq transfers)
                           (set-lowest-fetched-block checksum transfers))]

                  (seq transfers)
                  (concat (mapv add-transfer transfers))

                  true
                  (conj (wallet/update-balances
                         (into [] (reduce (fn [acc {:keys [address block]}]
                                            (if (>= block min-known-block)
                                              (conj acc address)
                                              acc))
                                          #{}
                                          transfers))))

                  (< (count transfers) limit)
                  (conj (tx-history-end-reached checksum)))]
    (apply fx/merge cofx (tx-fetching-ended [checksum]) effects)))

(fx/defn tx-fetching-failed
  {:events [::tx-fetching-failed]}
  [cofx error address]
  (log/debug "[transactions] tx-fetching-failed"
             "address" address
             "error" error)
  (tx-fetching-ended cofx [address]))

(re-frame/reg-fx
 :transactions/get-transfers
 (fn [{:keys [chain-tokens addresses before-block limit
              limit-per-address]
       :as params
       :or {limit default-transfers-limit}}]
   {:pre [(cljs.spec.alpha/valid?
           (cljs.spec.alpha/coll-of string?)
           addresses)]}
   (log/debug "[transactions] get-transfers"
              "addresses" addresses
              "block" before-block
              "limit" limit
              "limit-per-address" limit-per-address)
   (doseq [address addresses]
     (let [limit (or (get limit-per-address address)
                     limit)]
       (json-rpc/call
        {:method "wallet_getTransfersByAddress"
         :params [address (encode/uint before-block) (encode/uint limit)]
         :on-success #(re-frame/dispatch
                       [::new-transfers
                        (enrich-transfers chain-tokens %)
                        (assoc params :address address
                               :limit limit)])
         :on-error #(re-frame/dispatch [::tx-fetching-failed address])})))))

(fx/defn fetch-more-tx
  {:events [:transactions/fetch-more]}
  [{:keys [db] :as cofx} address]
  (let [min-known-block (or (get-min-known-block db address)
                            (:ethereum/current-block db))
        min-block-transfers-count (or (min-block-transfers-count db address) 0)]
    (fx/merge
     cofx
     {:transactions/get-transfers
      {:chain-tokens          (:wallet/all-tokens db)
       :addresses             [address]
       :before-block          min-known-block
       :historical?           true
       ;; Transfers are requested before and including `min-known-block` because
       ;; there is no guarantee that all transfers from that block are shown
       ;; already. To make sure that we fetch the whole `default-transfers-limit`
       ;; of transfers the number of transfers already received for
       ;; `min-known-block` is added to the page size.
       :limit-per-address {address (+ default-transfers-limit
                                      min-block-transfers-count)}}}
     (tx-fetching-in-progress [address]))))
