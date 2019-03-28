(ns status-im.utils.ethereum.stickers
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.abi-spec :as abi-spec]))

(def contracts
  {:mainnet nil
   :testnet "0x39d16CdB56b5a6a89e1A397A13Fe48034694316E"
   :rinkeby nil})

(defn pack-count
  "Returns number of packs rigestered in the contract"
  [contract cb]
  (ethereum/call (ethereum/call-params contract "packCount()")
                 (fn [count] (cb (ethereum/hex->int count)))))

(defn pack-data
  "Returns vector of pack data parameters by pack id: [category owner mintable timestamp price contenthash]"
  [contract pack-id cb]
  (ethereum/call (ethereum/call-params contract "getPackData(uint256)" (ethereum/int->hex pack-id))
                 (fn [data]
                   (cb (abi-spec/decode data ["bytes4[]" "address" "bool" "uint256" "uint256" "bytes"])))))

(defn owned-tokens
  "Returns vector of owned tokens ids in the contract by address"
  [contract address cb]
  (ethereum/call (ethereum/call-params contract "tokensOwnedBy(address)" (ethereum/normalized-address address))
                 (fn [data]
                   (cb (first (abi-spec/decode data ["uint256[]"]))))))

(defn token-pack-id
  "Returns pack id in the contract by token id"
  [contract token cb]
  (ethereum/call (ethereum/call-params contract "tokenPackId(uint256)" (ethereum/int->hex token))
                 (fn [data] (cb (ethereum/hex->int data)))))
