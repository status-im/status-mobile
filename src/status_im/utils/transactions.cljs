(ns status-im.utils.transactions
  (:require [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [status-im.utils.money :as money]))

(defn get-transaction-url [network account]
  (let [network (case network
                  "testnet" "ropsten"
                  "mainnet" "api")]
    (str "https://" network ".etherscan.io/api?module=account&action=txlist&address=0x"
         account "&startblock=0&endblock=99999999&sort=desc&apikey=YourApiKeyToken?q=json")))

(defn format-transaction [account {:keys [value to from timeStamp]}]
  (let [transaction {:value (money/wei->ether value)
                     ;; timestamp is in seconds, we convert it in ms
                     :timestamp (str timeStamp "000")
                     :symbol "ETH"}
        inbound?    (= (str "0x" account) to)]
    (if inbound?
      (assoc transaction
             :from from
             :type :inbound)
      (assoc transaction
             :to to
             :type :outbound))))

(defn format-transactions-response [response account]
  (->> response
       types/json->clj
       :result
       (map (partial format-transaction account))
       (sort-by :timestamp)))

(defn get-transactions [network account on-success on-error]
  (utils/http-get (get-transaction-url network account)
                  #(on-success (format-transactions-response % account))
                  on-error))
