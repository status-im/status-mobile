(ns status-im.ethereum.stateofus
  (:require [clojure.string :as string]))

(def domain "stateofus.eth")

(defn- subdomain [username]
  (str username "." domain))

(def registrars
  {:mainnet "0xDB5ac1a559b02E12F29fC0eC0e37Be8E046DEF49"
   :testnet "0x11d9F481effd20D76cEE832559bd9Aca25405841"})

(defn- lower-case? [s]
  (when s
    (= s (string/lower-case s))))

(defn valid-username? [username]
  (and (< 4 (count username))
       (lower-case? username)
       (re-find #"^[a-z]+$" username)))