(ns utils.ens.stateofus
  (:require
    [clojure.string :as string]
    [utils.ens.core :as utils.ens]))

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
     (if (utils.ens/is-valid-eth-name? contact-identity)
       contact-identity
       (subdomain contact-identity)))))
