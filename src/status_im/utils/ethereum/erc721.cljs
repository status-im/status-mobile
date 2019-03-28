(ns status-im.utils.ethereum.erc721
  "
  Helper functions to interact with [ERC721](https://eips.ethereum.org/EIPS/eip-721) smart contract
  "
  (:require [status-im.utils.ethereum.core :as ethereum]))

(defn token-of-owner-by-index [contract address index cb]
  (ethereum/call (ethereum/call-params
                  contract
                  "tokenOfOwnerByIndex(address,uint256)"
                  (ethereum/normalized-address address)
                  (ethereum/int->hex index))
                 #(cb (ethereum/hex->bignumber %))))

(defn token-uri [contract tokenId cb]
  (ethereum/call (ethereum/call-params
                  contract
                  "tokenURI(uint256)"
                  (ethereum/int->hex tokenId))
                 #(cb (ethereum/hex->string %))))
