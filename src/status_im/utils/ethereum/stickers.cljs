(ns status-im.utils.ethereum.stickers
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.abi-spec :as abi-spec]))

(def contracts
  {:mainnet nil
   :testnet "0x39d16CdB56b5a6a89e1A397A13Fe48034694316E"
   :rinkeby nil})

(defn pack-count
  "Returns number of packs rigestered in the contract"
  [web3 contract cb]
  (ethereum/call web3
                 (ethereum/call-params contract "packCount()")
                 (fn [_ count] (cb (ethereum/hex->int count)))))

(defn pack-data
  "Returns vector of pack data parameters by pack id: [category owner mintable timestamp price contenthash]"
  [web3 contract pack-id cb]
  (ethereum/call web3
                 (ethereum/call-params contract "getPackData(uint256)" (ethereum/int->hex pack-id))
                 (fn [_ data]
                   (cb (when data (abi-spec/decode (subs data 2) ["bytes4[]" "address" "bool" "uint256" "uint256" "bytes"]))))))

(defn owned-tokens
  "Returns vector of owned tokens ids in the contract by address"
  [web3 contract address cb]
  (ethereum/call web3
                 (ethereum/call-params contract "tokensOwnedBy(address)" (ethereum/normalized-address address))
                 (fn [_ data]
                   (cb (when data (first (abi-spec/decode (subs data 2) ["uint256[]"])))))))

(defn token-pack-id
  "Returns pack id in the contract by token id"
  [web3 contract token cb]
  (ethereum/call web3
                 (ethereum/call-params contract "tokenPackId(uint256)" (ethereum/int->hex token))
                 (fn [_ data] (cb (ethereum/hex->int data)))))
