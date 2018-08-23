(ns status-im.utils.ethereum.ens
  "
  https://docs.ens.domains/en/latest/index.html
  https://eips.ethereum.org/EIPS/eip-137
  https://eips.ethereum.org/EIPS/eip-181
  "
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]))

;; this is the addresses of ens registries for the different networks
(def ens-registries
  {:mainnet "0x314159265dd8dbb310642f98f50c066173c1259b"
   :testnet "0x112234455c3a32fd11230c42e7bccd4a84e02010"
   :rinkeby "0xe7410170f87102DF0055eB195163A03B7F2Bff4A"})

(def default-namehash "0000000000000000000000000000000000000000000000000000000000000000")

(defn namehash [s]
  (ethereum/normalized-address (if (string/blank? s)
                                 default-namehash
                                 (let [[label remainder] (-> s
                                                             string/lower-case
                                                             (string/split #"\." 2))]
                                   (ethereum/sha3 (+ (namehash remainder)
                                                     (subs (ethereum/sha3 label) 2))
                                                  {:encoding "hex"})))))

;; Registry contract

(defn resolver [web3 registry ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params registry
                                       "resolver(bytes32)"
                                       (namehash ens-name))
                 (fn [_ address] (cb  (ethereum/hex->address address)))))

(defn owner [web3 registry ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params registry
                                       "owner(bytes32)"
                                       (namehash ens-name))
                 (fn [_ address] (cb address))))

(defn ttl [web3 registry ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params registry
                                       "ttl(bytes32)"
                                       (namehash ens-name))
                 (fn [_ ttl] (cb (ethereum/hex->int ttl)))))

;; Resolver contract
;; Resolver must implement EIP65 (supportsInterface). When interacting with an unknown resolver it's safer to rely on it.

(def addr-hash "0x3b3b57de")

(defn addr [web3 resolver ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver "addr(bytes32)" (namehash ens-name))
                 (fn [_ address] (cb (ethereum/hex->address address)))))

(def name-hash "0x691f3431")

;; Defined by https://eips.ethereum.org/EIPS/eip-181

(defn name [web3 resolver ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver
                                       "name(bytes32)"
                                       (namehash ens-name))
                 (fn [_ address] (cb (ethereum/hex->address address)))))

(def ABI-hash "0x2203ab56")
(def pubkey-hash "0xc8690233")

(defn add-uncompressed-public-key-prefix [key]
  (when key
    (str "0x04" (subs key 2))))

(defn pubkey [web3 resolver ens-name cb]
  (ethereum/call web3
                 (ethereum/call-params resolver
                                       "pubkey(bytes32)"
                                       (namehash ens-name))
                 (fn [_ key] (cb (add-uncompressed-public-key-prefix key)))))

;; TODO ABI, pubkey
