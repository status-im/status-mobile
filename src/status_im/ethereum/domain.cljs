(ns status-im.ethereum.domain
  (:require [clojure.string :as string]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.uns :as uns]))

;; this is the addresses of ens registries for the different networks
(def ens-registries
  {:mainnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :goerli  "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"})

(def default-address "0x0000000000000000000000000000000000000000")
(def default-key "0x0400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")

(defn valid-eth-name-prefix?
  [prefix]
  (not
   (or (string/blank? prefix)
       (string/ends-with? prefix ".")
       (string/includes? prefix ".."))))

(defn is-valid-domain-name?
  [domain-name]
  (and domain-name
       (or (ens/is-valid-eth-name? domain-name)
           (uns/is-valid-uns-name? domain-name))))

(defn is-valid-eth-name?
  [ens-name]
  (and ens-name
       (string? ens-name)
       (string/ends-with? ens-name ".eth")))

(defn address
  [chain-id domain-name cb]
  {:pre [(is-valid-domain-name? domain-name)]}
  (if (ens/is-valid-eth-name? domain-name) (ens/address chain-id domain-name cb) (uns/address domain-name cb)))

(defn owner
  [chain-id domain-name cb]
  {:pre [(is-valid-domain-name? domain-name)]}
  (if (ens/is-valid-eth-name? domain-name) (ens/owner chain-id domain-name cb) (uns/owner domain-name cb)))

(defn pubkey
  [chain-id domain-name cb]
  {:pre [(ens/is-valid-eth-name? domain-name)]}
  (ens/pubkey chain-id domain-name cb))

(defn resource-url
  [chain-id domain-name cb]
  {:pre [(ens/is-valid-eth-name? domain-name)]}
  (ens/resource-url chain-id domain-name cb))

(defn expire-at
  [chain-id domain-name cb]
  {:pre [(ens/is-valid-eth-name? domain-name)]}
  (ens/expire-at chain-id domain-name cb))

(defn register-prepare-tx
  [chain-id from domain-name pubkey cb]
  {:pre [(ens/is-valid-eth-name? domain-name)]}
  (ens/register-prepare-tx chain-id from domain-name pubkey cb))

(defn set-pub-key-prepare-tx
  [chain-id from domain-name pubkey cb]
  {:pre [(ens/is-valid-eth-name? domain-name)]}
  (ens/set-pub-key-prepare-tx chain-id from domain-name pubkey cb))