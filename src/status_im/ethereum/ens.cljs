(ns status-im.ethereum.ens
  "
  https://docs.ens.domains/en/latest/index.html
  https://eips.ethereum.org/EIPS/eip-137
  https://eips.ethereum.org/EIPS/eip-181
  "
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.abi-spec :as abi-spec]))

;; this is the addresses of ens registries for the different networks
(def ens-registries
  {:mainnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :testnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :rinkeby "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"})

(def default-namehash "0000000000000000000000000000000000000000000000000000000000000000")
(def default-address "0x0000000000000000000000000000000000000000")
(def default-key "0x0400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")

(defn namehash
  [s]
  (ethereum/normalized-hex
   (if (string/blank? s)
     default-namehash
     (let [[label remainder] (-> s
                                 string/lower-case
                                 (string/split #"\." 2))]
       (if-not (seq label)
         default-namehash
         (ethereum/sha3 (str (namehash remainder)
                             (subs (ethereum/sha3 label) 2))))))))

(defn resolver
  [registry ens-name cb]
  (json-rpc/eth-call
   {:contract registry
    :method "resolver(bytes32)"
    :params [(namehash ens-name)]
    :outputs ["address"]
    :on-error #(cb "0x")
    :on-success
    (fn [[address]]
      (cb (when-not (= address default-address) address)))}))

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

(defn address
  [chain-id ens-name cb]
  {:pre [(is-valid-eth-name? ens-name)]}
  (json-rpc/call {:method     "ens_addressOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  :on-error   #(cb "0x")}))

(defn pubkey
  [chain-id ens-name cb]
  {:pre [(is-valid-eth-name? ens-name)]}
  (json-rpc/call {:method     "ens_publicKeyOf"
                  :params     [chain-id ens-name]
                  :on-success (fn [result]
                                (cb (str "0x04" (subs result 2))))
                  ;;at some point infura started to return execution reverted error instead of "0x" result
                  ;;our code expects "0x" result
                  :on-error #(cb "0x")}))

(defn owner
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_ownerOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  :on-error   #(cb "0x")}))

(defn resource-url
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_resourceURL"
                  :params     [chain-id ens-name]
                  :on-success #(cb (str "https://" (:Host %)))
                  :on-error   #(cb "0x")}))

(defn expire-at
  [chain-id ens-name cb]
  (json-rpc/call {:method "ens_expireAt"
                  :params [chain-id ens-name]
                  :on-success
                  ;;NOTE: returns a timestamp in s and we want ms
                  #(cb (* (js/Number (abi-spec/hex-to-number %)) 1000))}))