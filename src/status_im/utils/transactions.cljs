(ns status-im.utils.transactions
  (:require [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [status-im.utils.money :as money]))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn get-network-subdomain [network]
  (case network
    ("testnet" "testnet_rpc") "ropsten"
    ("mainnet" "mainnet_rpc") "api"
    ("rinkeby" "rinkeby_rpc") "rinkeby"))

(defn get-transaction-details-url [network hash]
  (let [network-subdomain (get-network-subdomain network)]
    (str "https://" network-subdomain ".etherscan.io/tx/" hash)))

(defn get-transaction-url [network account]
  (let [network-subdomain (get-network-subdomain network)]
    (str "https://" network-subdomain ".etherscan.io/api?module=account&action=txlist&address=0x"
         account "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key "?q=json")))

(defn- format-transaction [account {:keys [value timeStamp blockNumber hash from to gas gasPrice gasUsed nonce confirmations input]}]
  (let [inbound?    (= (str "0x" account) to)]
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
  (utils/http-get (get-transaction-url network account)
                  #(on-success (format-transactions-response % account))
                  on-error))
