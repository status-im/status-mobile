(ns status-im.utils.ethereum.ens
  "
  https://docs.ens.domains/en/latest/index.html
  https://eips.ethereum.org/EIPS/eip-137
  https://eips.ethereum.org/EIPS/eip-181
  "
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]))

(def registries
  {:mainnet "0x314159265dd8dbb310642f98f50c066173c1259b"
   :ropsten "0x112234455c3a32fd11230c42e7bccd4a84e02010"
   :rinkeby "0xe7410170f87102DF0055eB195163A03B7F2Bff4A"})

(def default-namehash "0000000000000000000000000000000000000000000000000000000000000000")

(defn namehash [s]
  (if (string/blank? s)
    default-namehash
    (let [[label remainder] (string/split s #"\." 2)]
      (ethereum/sha3 (+ (namehash remainder) (subs (ethereum/sha3 label) 2)) {:encoding "hex"}))))

;; Registry contract

(defn resolver [web3 registry name cb]
  (ethereum/call web3
                 (ethereum/call-params registry "resolver(bytes32)" (namehash name))
                 #(cb %1 (ethereum/hex->string %2))))

(defn owner [web3 registry name cb]
  (ethereum/call web3
                 (ethereum/call-params registry "owner(bytes32)" (namehash name))
                 #(cb %1 (ethereum/hex->string %2))))

(defn ttl [web3 registry name cb]
  (ethereum/call web3
                 (ethereum/call-params registry "ttl(bytes32)" (namehash name))
                 #(cb %1 (ethereum/hex->int %2))))

;; Resolver contract

;; Resolver must implement EIP65 (supportsInterface). When interacting with an unknown resolver it's safer to rely on it.

(def addr-hash "0x3b3b57de")

(defn addr [web3 resolver name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver "addr(bytes32)" (namehash name))
                 #(cb %1 %2)))

(defn content [web3 resolver name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver "content(bytes32)" (namehash name))
                 #(cb %1 (ethereum/hex->string %2))))

(def name-hash "0x691f3431")

;; Defined by https://eips.ethereum.org/EIPS/eip-181

(defn name [web3 resolver name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver "name(bytes32)" (namehash name))
                 #(cb %1 (ethereum/hex->string %2))))

(defn text [web3 resolver name key cb]
  (ethereum/call web3
                 (ethereum/call-params resolver "text(bytes32,string)" (namehash name) key)
                 #(cb %1 (ethereum/hex->string %2))))

(def ABI-hash "0x2203ab56")
(def pubkey-hash "0xc8690233")

;; TODO ABI, pubkey
