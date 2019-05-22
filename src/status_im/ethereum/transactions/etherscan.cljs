(ns status-im.ethereum.transactions.etherscan
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

;; --------------------------------------------------------------------------
;; etherscan transactions
;; --------------------------------------------------------------------------

(def etherscan-supported? #{:testnet :mainnet :rinkeby})

(let [network->subdomain {:testnet "ropsten" :rinkeby "rinkeby"}]
  (defn get-transaction-details-url [chain hash]
    {:pre [(keyword? chain) (string? hash)]
     :post [(or (nil? %) (string? %))]}
    (when (etherscan-supported? chain)
      (let [network-subdomain (when-let [subdomain (network->subdomain chain)]
                                (str subdomain "."))]
        (str "https://" network-subdomain "etherscan.io/tx/" hash)))))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn- get-api-network-subdomain [chain]
  (case chain
    (:testnet) "api-ropsten"
    (:mainnet) "api"
    (:rinkeby) "api-rinkeby"))

(defn- get-transaction-url
  ([chain address] (get-transaction-url chain address false))
  ([chain address chaos-mode?]
   {:pre  [(keyword? chain) (string? address)]
    :post [(string? %)]}
   (let [network-subdomain (get-api-network-subdomain chain)]
     (if chaos-mode?
       "http://httpstat.us/500"
       (str "https://" network-subdomain
            ".etherscan.io/api?module=account&action=txlist&address=" address
            "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key
            "&q=json")))))

(defn- get-token-transaction-url
  ([chain address] (get-token-transaction-url chain address false))
  ([chain address chaos-mode?]
   {:pre  [(keyword? chain) (string? address)]
    :post [(string? %)]}
   (let [network-subdomain (get-api-network-subdomain chain)]
     (if chaos-mode?
       "http://httpstat.us/500"
       (str "https://" network-subdomain
            ".etherscan.io/api?module=account&action=tokentx&address=" address
            "&startblock=0&endblock=999999999&sort=asc&apikey=" etherscan-api-key
            "&q=json")))))

(defn- format-transaction
  [address
   {:keys [value timeStamp blockNumber hash from to
           gas gasPrice gasUsed nonce input isError]}]
  (let [inbound? (= address to)
        error?   (= "1" isError)]
    {:value         value
     ;; timestamp is in seconds, we convert it in ms
     :timestamp     (str timeStamp "000")
     :symbol        :ETH
     :type          (cond error?   :failed
                          inbound? :inbound
                          :else    :outbound)
     :block         blockNumber
     :hash          hash
     :from          from
     :to            to
     :gas-limit     gas
     :gas-price     gasPrice
     :gas-used      gasUsed
     :nonce         nonce
     :data          input}))

(defn- format-token-transaction
  [address
   chain-tokens
   {:keys [contractAddress blockHash hash tokenDecimal gasPrice value
           gas tokenName timeStamp transactionIndex tokenSymbol
           confirmations blockNumber from gasUsed input nonce
           cumulativeGasUsed to]}]
  (let [inbound? (= address to)
        token    (get chain-tokens contractAddress
                      {:name tokenName
                       :symbol tokenSymbol
                       :decimals tokenDecimal
                       :address contractAddress})]
    {:value         value
     ;; timestamp is in seconds, we convert it in ms
     :timestamp     (str timeStamp "000")
     :symbol        (keyword tokenSymbol)
     :type          (if inbound?
                      :inbound
                      :outbound)
     :block         blockNumber
     :hash          hash
     :from          from
     :to            to
     :gas-limit     gas
     :gas-price     gasPrice
     :gas-used      gasUsed
     :nonce         nonce
     :data          input
     :error?        false
     :transfer      true
     :token         token}))

(defn- format-transactions-response [response format-fn]
  (let [{:keys [result]} (types/json->clj response)]
    (cond-> {}
      (vector? result)
      (into (comp
             (map format-fn)
             (map (juxt :hash identity)))
            result))))

(defn- etherscan-history
  [chain address on-success on-error chaos-mode?]
  (if (etherscan-supported? chain)
    (let [url       (get-transaction-url chain address chaos-mode?)]
      (log/debug :etherscan-transactions :url url)
      (http/get url
                #(on-success (format-transactions-response
                              %
                              (partial format-transaction address)))
                on-error))
    (log/info "Etherscan not supported for " chain)))

(defn- etherscan-token-history
  [chain address chain-tokens on-success on-error chaos-mode?]
  (if (etherscan-supported? chain)
    (let [token-url (get-token-transaction-url chain address chaos-mode?)]
      (log/debug :etherscan-token-transactions :token-url token-url)
      (http/get token-url
                #(on-success (format-transactions-response
                              %
                              (partial format-token-transaction address chain-tokens)))
                on-error))
    (log/info "Etherscan not supported for " chain)))

(re-frame/reg-fx
 :ethereum.transactions.etherscan/fetch-history
 (fn [{:keys [chain address on-success on-error chaos-mode?]}]
   (etherscan-history chain address on-success on-error chaos-mode?)))

(re-frame/reg-fx
 :ethereum.transactions.etherscan/fetch-token-history
 (fn [{:keys [chain chain-tokens address on-success on-error chaos-mode?]}]
   (etherscan-token-history chain address chain-tokens on-success on-error chaos-mode?)))

;; -----------------------------------------------
;; chain transactions
;; -----------------------------------------------

(fx/defn fetch-history
  [{:keys [db] :as cofx}]
  (let [{:keys [:account/account :wallet/all-tokens]} db
        chain (ethereum/chain-keyword db)
        chain-tokens (into {} (map (juxt :address identity)
                                   (tokens/tokens-for all-tokens chain)))
        chaos-mode? (get-in account [:settings :chaos-mode?])
        normalized-address (ethereum/current-address db)]
    #:ethereum.transactions.etherscan
     {:fetch-history
      {:chain chain
       :address normalized-address
       :on-success
       #(re-frame/dispatch
         [:ethereum.transactions.callback/fetch-history-success %])
       :on-error
       #(re-frame/dispatch
         [:ethereum.transactions.callback/etherscan-error %])
       :chaos-mode? chaos-mode?}
      :fetch-token-history
      {:chain chain
       :chain-tokens chain-tokens
       :address normalized-address
       :on-success
       #(re-frame/dispatch
         [:ethereum.transactions.callback/fetch-token-history-success %])
       :on-error
       #(re-frame/dispatch
         [:ethereum.transactions.callback/etherscan-error %])
       :chaos-mode? chaos-mode?}}))
