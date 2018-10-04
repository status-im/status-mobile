(ns status-im.utils.transactions
  (:require [status-im.utils.ethereum.core :as ethereum]
            [cljs-time.core :as t]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn- get-network-subdomain [chain]
  (case chain
    (:testnet) "ropsten"
    (:mainnet) nil
    (:rinkeby) "rinkeby"))

(defn get-transaction-details-url [chain hash]
  (let [network-subdomain (get-network-subdomain chain)]
    (str "https://" (when network-subdomain (str network-subdomain ".")) "etherscan.io/tx/" hash)))

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

;; web3 experiments

(defn- format-transaction-details-web3 [currentBlockNumber account timestamp blockNumber {:keys [hash from to gas gasPrice gasUsed nonce input value]}]
  (let [inbound? (= (str "0x" account) to)
        confirmations (- currentBlockNumber blockNumber)]
    {:value         (.toFixed value)
    ;; timestamp is in seconds, we convert it in ms
     :timestamp     (str timestamp "000")
     :symbol        :ETH
     :type          (cond inbound? :inbound
                          :else    :outbound)
     :block         (.toFixed blockNumber)
     :hash          hash
     :from          from
     :to            to
     :gas-limit     (.toFixed gas)
     :gas-price     (.toFixed gasPrice)
     :gas-used      gasUsed
     :nonce         (.toFixed nonce)
     :confirmations confirmations
     :data          input}))

(defn- filter-txs-by-account [account txs]
  (filter
   (fn [{:keys [to from]}]
     (let [acc (str "0x" account)]
       (or (= acc to) (= acc from))))
   txs))

(defn- format-transaction-web3 [currentBlockNumber account {:keys [transactions timestamp number]}]
  (reduce (fn [txs {:keys [hash] :as transaction}]
            (assoc txs hash (format-transaction-details-web3 currentBlockNumber account timestamp number transaction)))
          {} (filter-txs-by-account account transactions)))

(defn- get-transactions-web3 [web3 account currentBlockNumber blockNumber targetBlockNumber txs on-success]
  (do
    (println  "getting block info:" blockNumber " - txs found so far: " (count txs))
    (if (>= blockNumber targetBlockNumber)
      (ethereum/get-block-info-with-txs
       web3
       blockNumber
       (fn [blockInfo]
         (get-transactions-web3 web3 account currentBlockNumber (dec blockNumber) targetBlockNumber (merge txs (format-transaction-web3 currentBlockNumber account blockInfo)) on-success)))
      (if (< blockNumber targetBlockNumber) (on-success txs)))))

(defn get-transactions [web3 chain account on-success on-error]
  (let [start (t/now)]
    (ethereum/get-block-number web3
                               (fn [currentBlockNumber]
                                 (do
                                   (println "currentBlockNumber: " currentBlockNumber)
                                   (get-transactions-web3 web3 account currentBlockNumber currentBlockNumber (- currentBlockNumber 50) {}
                                                          (fn [txs]
                                                            (do
                                                              (println "elapsed: " (t/in-millis (t/interval start (t/now))) "ms")
                                                              (on-success txs)))))))))
  ;;(let [url (get-transaction-url chain account)]
  ;;  (log/debug "HTTP GET" url)
  ;;  (http/get url
  ;;            #(println (format-transactions-response % account))
  ;;            on-error)))
