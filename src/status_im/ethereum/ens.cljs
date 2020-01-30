(ns status-im.ethereum.ens
  "
  https://docs.ens.domains/en/latest/index.html
  https://eips.ethereum.org/EIPS/eip-137
  https://eips.ethereum.org/EIPS/eip-181
  "
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]))

;; this is the addresses of ens registries for the different networks
(def ens-registries
  {:mainnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :testnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :rinkeby "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"})

(def default-namehash "0000000000000000000000000000000000000000000000000000000000000000")
(def default-address "0x0000000000000000000000000000000000000000")
(def default-key "0x0400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")

(defn namehash
  [s]
  (ethereum/normalized-hex
   (if (string/blank? s)
     default-namehash
     (let [[label remainder] (-> s
                                 string/lower-case
                                 (string/split #"\." 2))]
       (ethereum/sha3 (+ (namehash remainder)
                         (subs (ethereum/sha3 label) 2)))))))

;; Registry contract

(defn resolver
  [registry ens-name cb]
  (json-rpc/eth-call
   {:contract registry
    :method "resolver(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["address"]
    :on-success
    (fn [[address]]
      (cb (when-not (= address default-address) address)))}))

(defn owner
  [registry ens-name cb]
  (json-rpc/eth-call
   {:contract registry
    :method "owner(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["address"]
    :on-success
    (fn [[address]]
      (cb address))}))

(defn ttl
  [registry ens-name cb]
  (json-rpc/eth-call
   {:contract registry
    :method "ttl(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["uint256"]
    :on-success
    (fn [[ttl]]
      (cb ttl))}))

;; Resolver contract
;; Resolver must implement EIP65 (supportsInterface). When interacting with an unknown resolver it's safer to rely on it.

(def addr-hash "0x3b3b57de")

(defn addr
  [resolver ens-name cb]
  (json-rpc/eth-call
   {:contract resolver
    :method "addr(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["address"]
    :on-success
    (fn [[address]]
      (cb address))}))

(def name-hash "0x691f3431")

;; Defined by https://eips.ethereum.org/EIPS/eip-181

(defn name
  [resolver ens-name cb]
  (json-rpc/eth-call
   {:contract resolver
    :method "name(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["string"]
    :on-success
    (fn [[name]]
      (cb name))}))

(defn contenthash
  [resolver ens-name cb]
  (json-rpc/eth-call
   {:contract resolver
    :method "contenthash(bytes32)"
    :params [(namehash ens-name)]
    :on-success
    (fn [raw-hash]
      ;; NOTE: it would be better if our abi-spec/decode was able to do that
      (let [hash (subs raw-hash 130)
            [cid & hash-and-zeros] (string/split hash "1b20")
            hash (str "0x" cid "1b20" (subs (apply str hash-and-zeros) 0 64))]
        (cb hash)))}))

(defn content
  [resolver ens-name cb]
  (json-rpc/eth-call
   {:contract resolver
    :method "content(bytes32)"
    :params [(namehash ens-name)]
    :on-success
    (fn [hash]
      (cb hash))}))

(def ABI-hash "0x2203ab56")
(def pubkey-hash "0xc8690233")

(defn uncompressed-public-key
  [x y]
  (when (and x y)
    (str "0x04" x y)))

(defn valid-eth-name-prefix?
  [prefix]
  (not
   (or (string/blank? prefix)
       (string/ends-with? prefix ".")
       (string/includes? prefix ".."))))

(defn is-valid-eth-name?
  [ens-name]
  (and ens-name
       (string? ens-name)
       (string/ends-with? ens-name ".eth")))

(defn pubkey
  [resolver ens-name cb]
  (json-rpc/eth-call
   {:contract resolver
    :method "pubkey(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["bytes32" "bytes32"]
    :on-success
    (fn [[x y]]
      (let [public-key (uncompressed-public-key x y)]
        (cb public-key)))}))

(defn get-addr
  [registry ens-name cb]
  {:pre [(is-valid-eth-name? ens-name)]}
  (resolver registry
            ens-name
            #(addr % ens-name cb)))

(defn get-owner
  [registry ens-name cb]
  {:pre [(is-valid-eth-name? ens-name)]}
  (owner registry ens-name cb))
