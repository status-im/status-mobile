(ns status-im.utils.ethereum.erc721
  "
  Helper functions to interact with [ERC721](https://eips.ethereum.org/EIPS/eip-721) smart contract
  "
  (:require [status-im.utils.ethereum.core :as ethereum]))

(defn token-of-owner-by-index [web3 contract address index cb]
  (ethereum/call web3
                 (ethereum/call-params
                  contract
                  "tokenOfOwnerByIndex(address,uint256)"
                  (ethereum/normalized-address address)
                  (ethereum/int->hex index))
                 #(cb %1 (ethereum/hex->bignumber %2))))
