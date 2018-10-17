(ns status-im.utils.transactions
  (:require [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def etherscan-supported?
  #{:testnet :mainnet :rinkeby})

(defn- get-network-subdomain [chain]
  (case chain
    (:testnet) "ropsten"
    (:mainnet) nil
    (:rinkeby) "rinkeby"))

(defn get-transaction-details-url [chain hash]
  (when (etherscan-supported? chain)
    (let [network-subdomain (get-network-subdomain chain)]
      (str "https://" (when network-subdomain (str network-subdomain ".")) "etherscan.io/tx/" hash))))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn- get-api-network-subdomain [chain]
  (case chain
    (:testnet) "api-ropsten"
    (:mainnet) "api"
    (:rinkeby) "api-rinkeby"))

(defn get-transaction-url [chain account]
  (let [network-subdomain (get-api-network-subdomain chain)]
    (str "https://" network-subdomain ".etherscan.io/api?module=account&action=txlist&address=0x"
         account "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key "?q=json")))

(defn- format-transaction [account {:keys [value timeStamp blockNumber hash from to gas gasPrice gasUsed nonce confirmations input isError]}]
  (let [inbound? (= (str "0x" account) to)
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
     :confirmations confirmations
     :data          input}))

(defn- format-transactions-response [response account]
  (->> response
       types/json->clj
       :result
       (reduce (fn [transactions {:keys [hash] :as transaction}]
                 (assoc transactions hash (format-transaction account transaction)))
               {})))

(defn get-transactions [chain account on-success on-error]
  (if (etherscan-supported? chain)
    (let [url (get-transaction-url chain account)]
      (log/debug "HTTP GET" url)
      (http/get url
                #(on-success (format-transactions-response % account))
                on-error))
    (log/info "Etherscan not supported for " chain)))
