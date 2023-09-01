(ns status-im.ethereum.stateofus
  (:require [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im2.config :as config]))

(def domain "stateofus.eth")

(defn subdomain
  [username]
  (str username "." domain))

(defn username-with-domain
  "checks if the username is a status username or a ens name
  for that we check if there is a dot in the username, which
  would indicate that there is already a domain name so we don't
  concatenated stateofus domain to it"
  [username]
  (when (and (string? username)
             (seq username))
    (if (string/includes? username ".")
      username
      (subdomain username))))

(defn username
  [name]
  (when (and name (string/ends-with? name domain))
    (first (string/split name "."))))

(def old-registrars
  (merge
   {:mainnet "0xDB5ac1a559b02E12F29fC0eC0e37Be8E046DEF49"}
   (when config/test-stateofus?
     {:goerli "0xD1f7416F91E7Eb93dD96A61F12FC092aD6B67B11"})))

(def registrars-cache (atom {}))

(defn get-registrar
  [chain callback]
  (if-let [contract (get @registrars-cache chain)]
    (callback contract)
    (ens/owner
     (ethereum/chain-keyword->chain-id chain)
     domain
     (fn [addr]
       (let [addr (or addr (get old-registrars chain))]
         (swap! registrars-cache assoc chain addr)
         (callback addr))))))

(defn get-cached-registrar
  [chain]
  (get @registrars-cache chain (get old-registrars chain)))

(defn lower-case?
  [s]
  (when s
    (= s (string/lower-case s))))

(defn valid-username?
  [s]
  (boolean (and (lower-case? s)
                (re-find #"^[a-z0-9]+$" s))))

(defn ens-name-parse
  [contact-identity]
  (when (string? contact-identity)
    (string/lower-case
     (if (ens/is-valid-eth-name? contact-identity)
       contact-identity
       (subdomain contact-identity)))))
