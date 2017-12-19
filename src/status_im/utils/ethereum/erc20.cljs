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
  (:require [status-im.utils.ethereum.core :as ethereum])
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
                 (ethereum/call-params contract "balanceOf(address)" address)
                 #(cb %1 (ethereum/hex->bignumber %2))))

(defn transfer [web3 contract address value cb]
  (ethereum/call web3
                 (ethereum/call-params contract "transfer(address, uint256)" address (ethereum/int->hex value))
                 #(cb %1 (ethereum/hex->boolean %2))))

(defn transfer-from [web3 contract from-address to-address value cb]
  (ethereum/call web3
                 (ethereum/call-params contract "transferFrom(address, address, uint256)" from-address to-address (ethereum/int->hex value))
                 #(cb %1 (ethereum/hex->boolean %2))))

(defn approve [web3 contract address value cb]
  (ethereum/call web3
                 (ethereum/call-params contract "approve(address, uint256)" address (ethereum/int->hex value))
                 #(cb %1 (ethereum/hex->boolean %2))))

(defn allowance [web3 contract owner-address spender-address cb]
  (ethereum/call web3
                 (ethereum/call-params contract "allowance(address, address)" owner-address spender-address)
                 #(cb %1 (ethereum/hex->bignumber %2))))