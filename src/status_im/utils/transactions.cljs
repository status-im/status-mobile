(ns status-im.utils.transactions
  (:require [status-im.utils.http :as http]
            [status-im.utils.types :as types]))

(defn- get-network-subdomain [network]
  (case network
    (:testnet) "ropsten"
    (:mainnet) nil
    (:rinkeby) "rinkeby"))

(defn get-transaction-details-url [network hash]
  (let [network-subdomain (get-network-subdomain network)]
    (str "https://" (when network-subdomain (str network-subdomain ".")) "etherscan.io/tx/" hash)))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn- get-api-network-subdomain [network]
  (case network
    (:testnet) "api-ropsten"
    (:mainnet) "api"
    (:rinkeby) "api-rinkeby"))

(defn get-transaction-url [network account]
  (let [network-subdomain (get-api-network-subdomain network)]
    (str "https://" network-subdomain ".etherscan.io/api?module=account&action=txlist&address=0x"
         account "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key "?q=json")))

(defn- format-transaction [account {:keys [value timeStamp blockNumber hash from to gas gasPrice gasUsed nonce confirmations input]}]
  (let [inbound? (= (str "0x" account) to)]
    {:value value
     ;; timestamp is in seconds, we convert it in ms
     :timestamp (str timeStamp "000")
     :symbol :ETH
     :type (if inbound? :inbound :outbound)
     :block blockNumber
     :hash  hash
     :from from
     :to to
     :gas-limit gas
     :gas-price gasPrice
     :gas-used gasUsed
     :nonce nonce
     :confirmations confirmations
     :data input}))

(defn- format-transactions-response [response account]
  (->> response
       types/json->clj
       :result
       (reduce (fn [transactions {:keys [hash] :as transaction}]
                 (assoc transactions hash (format-transaction account transaction)))
               {})))

(defn get-transactions [network account on-success on-error]
  (http/get (get-transaction-url network account)
            #(on-success (format-transactions-response % account))
            on-error))
