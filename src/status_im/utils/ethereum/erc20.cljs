(ns status-im.utils.ethereum.erc20
  "
  Helper functions to interact with [ERC20](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20-token-standard.md) smart contract

  Example

  Contract: https://ropsten.etherscan.io/address/0x29b5f6efad2ad701952dfde9f29c960b5d6199c5#readContract
  Owner: https://ropsten.etherscan.io/token/0x29b5f6efad2ad701952dfde9f29c960b5d6199c5?a=0xa7cfd581060ec66414790691681732db249502bd

  With a running node on Ropsten:
  (let [web3 (:web3 @re-frame.db/app-db)
        contract \"0x29b5f6efad2ad701952dfde9f29c960b5d6199c5\"
        address \"0xa7cfd581060ec66414790691681732db249502bd\"]
    (erc20/balance-of web3 contract address println))

  => 29166666
  "
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.native-module.core :as status]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.constants :as constants]
            [status-im.utils.datetime :as datetime]
            [clojure.string :as string]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types])
  (:refer-clojure :exclude [name symbol]))

(defn name [web3 contract cb]
  (ethereum/call web3 (ethereum/call-params contract "name()") cb))

(defn symbol [web3 contract cb]
  (ethereum/call web3 (ethereum/call-params contract "symbol()") cb))

(defn decimals [web3 contract cb]
  (ethereum/call web3 (ethereum/call-params contract "decimals()") cb))

(defn total-supply [web3 contract cb]
  (ethereum/call web3
                 (ethereum/call-params contract "totalSupply()")
                 #(cb %1 (ethereum/hex->bignumber %2))))

(defn balance-of [web3 contract address cb]
  (ethereum/call web3
                 (ethereum/call-params contract "balanceOf(address)" (ethereum/normalized-address address))
                 #(cb %1 (ethereum/hex->bignumber %2))))

(defn transfer [contract from to value gas gas-price masked-password on-completed]
  (status/send-transaction (types/clj->json
                            (merge (ethereum/call-params contract "transfer(address,uint256)" to value)
                                   {:from     from
                                    :gas      gas
                                    :gasPrice gas-price}))
                           (security/unmask masked-password)
                           on-completed))

(defn transfer-from [web3 contract from-address to-address value cb]
  (ethereum/call web3
                 (ethereum/call-params contract "transferFrom(address,address,uint256)" (ethereum/normalized-address from-address) (ethereum/normalized-address to-address) (ethereum/int->hex value))
                 #(cb %1 (ethereum/hex->boolean %2))))

(defn approve [web3 contract address value cb]
  (ethereum/call web3
                 (ethereum/call-params contract "approve(address,uint256)" (ethereum/normalized-address address)  (ethereum/int->hex value))
                 #(cb %1 (ethereum/hex->boolean %2))))

(defn allowance [web3 contract owner-address spender-address cb]
  (ethereum/call web3
                 (ethereum/call-params contract "allowance(address,address)" (ethereum/normalized-address owner-address) (ethereum/normalized-address spender-address))
                 #(cb %1 (ethereum/hex->bignumber %2))))

(defn- parse-json [s]
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      (if (= (:error res) "")
        {:result true}
        res))
    (catch :default e
      {:error (.-message e)})))

(defn- add-padding [address]
  (when address
    (str "0x000000000000000000000000" (subs address 2))))

(defn- remove-padding [topic]
  (if topic
    (str "0x" (subs topic 26))))

(defn- parse-transaction-entries [current-block-number block-info chain direction transfers]
  (into {}
        (keep identity
              (for [transfer transfers]
                (if-let [token (->> transfer :address (tokens/address->token chain))]
                  (when-not (:nft? token)
                    [(:transactionHash transfer)
                     {:block         (-> block-info :number str)
                      :hash          (:transactionHash transfer)
                      :symbol        (:symbol token)
                      :from          (-> transfer :topics second remove-padding)
                      :to            (-> transfer :topics last remove-padding)
                      :value         (-> transfer :data ethereum/hex->bignumber)
                      :type          direction

                      :confirmations (str (- current-block-number (-> transfer :blockNumber ethereum/hex->int)))

                      :gas-price     nil
                      :nonce         nil
                      :data          nil

                      :gas-limit     nil
                      :timestamp     (-> block-info :timestamp (* 1000) str)

                      :gas-used      nil

                      ;; NOTE(goranjovic) - metadata on the type of token: contains name, symbol, decimas, address.
                      :token         token

                      ;; NOTE(goranjovic) - if an event has been emitted, we can say there was no error
                      :error?        false

                      ;; NOTE(goranjovic) - just a flag we need when we merge this entry with the existing entry in
                      ;; the app, e.g. transaction info with gas details, or a previous transfer entry with old
                      ;; confirmations count.
                      :transfer      true}]))))))

(defn add-block-info [web3 current-block-number chain direction result success-fn]
  (let [transfers-by-block (group-by :blockNumber result)]
    (doseq [[block-number transfers] transfers-by-block]
      (ethereum/get-block-info web3 (ethereum/hex->int block-number)
                               (fn [block-info]
                                 (success-fn (parse-transaction-entries current-block-number
                                                                        block-info
                                                                        chain
                                                                        direction
                                                                        transfers)))))))

(defn- response-handler [web3 current-block-number chain direction error-fn success-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (add-block-info web3 current-block-number chain direction result success-fn)))))

;;
;; Here we are querying event logs for Transfer events.
;;
;; The parameters are as follows:
;; - address - token smart contract address
;; - fromBlock - we need to specify it, since default is latest
;; - topics[0] - hash code of the Transfer event signature
;; - topics[1] - address of token sender with leading zeroes padding up to 32 bytes
;; - topics[2] - address of token sender with leading zeroes padding up to 32 bytes
;;

(defn get-token-transfer-logs
  ;; NOTE(goranjovic): here we use direct JSON-RPC calls to get event logs because of web3 event issues with infura
  ;; we still use web3 to get other data, such as block info
  [web3 current-block-number chain contracts direction address cb]
  (let [[from to] (if (= :inbound direction)
                    [nil (ethereum/normalized-address address)]
                    [(ethereum/normalized-address address) nil])
        args {:jsonrpc "2.0"
              :id      2
              :method  constants/web3-get-logs
              :params  [{:address   (map string/lower-case contracts)
                         :fromBlock "0x0"
                         :topics    [constants/event-transfer-hash
                                     (add-padding from)
                                     (add-padding to)]}]}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (response-handler web3 current-block-number chain direction ethereum/handle-error cb))))

(defn get-token-transactions
  [web3 chain contracts direction address cb]
  (ethereum/get-block-number web3
                             #(get-token-transfer-logs web3 % chain contracts direction address cb)))
