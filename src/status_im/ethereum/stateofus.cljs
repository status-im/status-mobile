(ns status-im.ethereum.stateofus
  (:require [clojure.string :as string]))

(def domain "stateofus.eth")

(defn subdomain [username]
  (str username "." domain))

(defn username [name]
  (when (and name (string/ends-with? name domain))
    (first (string/split name "."))))

(def registrars
  {:mainnet "0xDB5ac1a559b02E12F29fC0eC0e37Be8E046DEF49"
   :testnet "0x11d9F481effd20D76cEE832559bd9Aca25405841"})

(defn lower-case? [s]
  (when s
    (= s (string/lower-case s))))

(defn valid-username? [username]
  (boolean
   (and (lower-case? username)
        (re-find #"^[a-z0-9]+$" username))))