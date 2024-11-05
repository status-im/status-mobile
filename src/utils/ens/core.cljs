(ns utils.ens.core
  (:require [clojure.string :as string]))

;; this is the addresses of ens registries for the different networks
(def ens-registries
  {:mainnet "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"
   :sepolia "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"})

(def default-address "0x0000000000000000000000000000000000000000")
(def default-key
  "0x0400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000")
(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")

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
