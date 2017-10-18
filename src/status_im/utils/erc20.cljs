(ns status-im.utils.erc20
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

;; Example
;;
;; Contract: https://ropsten.etherscan.io/address/0x29b5f6efad2ad701952dfde9f29c960b5d6199c5#readContract
;; Owner: https://ropsten.etherscan.io/token/0x29b5f6efad2ad701952dfde9f29c960b5d6199c5?a=0xa7cfd581060ec66414790691681732db249502bd
;;
;; With a running node on Ropsten:
;; (let [web3 (:web3 @re-frame.db/app-db)
;;       contract "0x29b5f6efad2ad701952dfde9f29c960b5d6199c5"
;;       address "0xa7cfd581060ec66414790691681732db249502bd"]
;; (erc20/balance-of web3 contract address println))
;;
;; => 0x0000000000000000000000000000000000000000000000000000000001bd0c4a
;; (hex->int "0x0000000000000000000000000000000000000000000000000000000001bd0c4a") ;; => 29166666 (note token decimals)

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype (str s)))

(defn hex->int [s]
  (js/parseInt s 16))

(defn zero-pad-64 [s]
  (str (apply str (drop (count s) (repeat 64 "0"))) s))

(defn format-param [param]
  (if (number? param)
    (zero-pad-64 (hex->int param))
    (zero-pad-64 (subs param 2))))

(defn format-call-params [method-id & params]
  (let [params (string/join (map format-param params))]
    (str method-id params)))

(defn get-call-params [contract method-id & params]
  (let [data (apply format-call-params method-id params)]
    {:to contract :data data}))

(defn sig->method-id [signature]
  (apply str (take 10 (sha3 signature))))

(defn balance-of-params [token of]
  (let [method-id (sig->method-id "balanceOf(address)")]
    (get-call-params token method-id of)))

(defn balance-of [web3 token of cb]
  (.call (.-eth web3)
         (clj->js (balance-of-params token of))
         cb))
