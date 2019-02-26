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
            [status-im.utils.security :as security]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.types :as types])
  (:refer-clojure :exclude [name symbol]))

(def utils dependencies/web3-utils)

(def snt-contracts
  {:mainnet "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
   :testnet "0xc55cF4B03948D7EBc8b9E8BAD92643703811d162"
   :rinkeby nil})

(def abi
  (clj->js
   [{:constant        true
     :inputs          []
     :name            "name"
     :outputs         [{:name ""
                        :type "string"}]
     :payable         false
     :stateMutability "view"
     :type            "function"}
    {:constant        true
     :inputs          []
     :name            "symbol"
     :outputs         [{:name ""
                        :type "string"}]
     :payable         false
     :stateMutability "view"
     :type            "function"}
    {:constant        true
     :inputs          []
     :name            "decimals"
     :outputs         [{:name ""
                        :type "uint8"}]
     :payable         false
     :stateMutability "view"
     :type            "function"}
    {:constant        true
     :inputs          [{:name "_who"
                        :type "address"}]
     :name            "balanceOf"
     :outputs         [{:name ""
                        :type "uint256"}]
     :payable         false
     :stateMutability "view"
     :type            "function"}
    {:constant        true
     :inputs          []
     :name            "totalSupply"
     :outputs         [{:name ""
                        :type "uint256"}],
     :payable         false
     :stateMutability "view"
     :type            "function"}
    {:constant        false
     :inputs          [{:name "_to"
                        :type "address"}
                       {:name "_value"
                        :type "uint256"}]
     :name            "transfer"
     :outputs         [{:name ""
                        :type "bool"}],
     :payable         false
     :stateMutability "nonpayable"
     :type            "function"}
    {:anonymous false
     :inputs    [{:indexed true
                  :name    "from"
                  :type    "address"},
                 {:indexed true
                  :name    "to"
                  :type    "address"},
                 {:indexed false
                  :name    "value"
                  :type    "uint256"}]
     :name      "Transfer"
     :type      "event"}]))

(defn get-instance* [web3 contract]
  (.at (.contract (.-eth web3) abi) contract))

(def get-instance
  (memoize get-instance*))

(defn name [web3 contract cb]
  (.name (get-instance web3 contract) cb))

(defn symbol [web3 contract cb]
  (.symbol (get-instance web3 contract) cb))

(defn decimals [web3 contract cb]
  (.decimals (get-instance web3 contract) cb))

(defn total-supply [web3 contract cb]
  (.totalSupply (get-instance web3 contract) cb))

(defn balance-of [web3 contract address cb]
  (.balanceOf (get-instance web3 contract) address cb))

(defn transfer [contract from to value gas gas-price masked-password on-completed]
  (status/send-transaction (types/clj->json
                            (merge (ethereum/call-params contract "transfer(address,uint256)" to value)
                                   {:from     from
                                    :gas      gas
                                    :gasPrice gas-price}))
                           (security/safe-unmask-data masked-password)
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
