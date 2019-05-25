(ns status-im.ethereum.erc721
  "
  Helper functions to interact with [ERC721](https://eips.ethereum.org/EIPS/eip-721) smart contract
  "
  (:require [status-im.ethereum.json-rpc :as json-rpc]))

(defn token-of-owner-by-index
  [contract address index cb]
  (json-rpc/eth-call
   {:contract contract
    :method "tokenOfOwnerByIndex(address,uint256)"
    :params [address index]
    :outputs ["uint256"]
    :on-success (fn [[token]] (cb token))}))

(defn token-uri
  [contract tokenId cb]
  (json-rpc/eth-call
   {:contract contract
    :method "tokenURI(uint256)"
    :params [tokenId]
    :outputs ["string"]
    :on-success (fn [[uri]] (cb uri))}))
