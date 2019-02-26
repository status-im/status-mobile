(ns status-im.utils.ethereum.stickers
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.abi-spec :as abi-spec]))

(def contracts
  {:mainnet nil
   :testnet "0x82694E3DeabE4D6f4e6C180Fe6ad646aB8EF53ae"
   :rinkeby nil})

(defn pack-count [web3 contract cb]
  "Returns number of packs rigestered in the contract"
  (ethereum/call web3
                 (ethereum/call-params contract "packCount()")
                 (fn [_ count] (cb (ethereum/hex->int count)))))

(defn pack-data [web3 contract pack-id cb]
  "Returns vector of pack data parameters by pack id: [category owner mintable timestamp price contenthash]"
  (ethereum/call web3
                 (ethereum/call-params contract "getPackData(uint256)" (ethereum/int->hex pack-id))
                 (fn [_ data]
                   (cb (abi-spec/decode (subs data 2) ["bytes4[]" "address" "bool" "uint256" "uint256" "bytes"])))))

(defn owned-tokens [web3 contract address cb]
  "Returns vector of owned tokens ids in the contract by address"
  (ethereum/call web3
                 (ethereum/call-params contract "tokensOwnedBy(address)" (ethereum/normalized-address address))
                 (fn [_ data]
                   (cb (first (abi-spec/decode (subs data 2) ["uint256[]"]))))))

(defn token-pack-id [web3 contract token cb]
  "Returns pack id in the contract by token id"
  (ethereum/call web3
                 (ethereum/call-params contract "tokenPackId(uint256)" (ethereum/int->hex token))
                 (fn [_ data] (cb (ethereum/hex->int data)))))