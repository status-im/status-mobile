(ns status-im.ethereum.uns
  (:require [clojure.string :as string]
            [status-im.utils.http :as http]
            [status-im.async-storage.core :as async-storage]))

(def uns-resolve-api "https://resolve.unstoppabledomains.com/domains/")
(def uns-tld-api "https://resolve.unstoppabledomains.com/supported_tlds")
(def api-key "b8e8e319-9ba8-4457-a790-76aa238c92a7")
(def header {"Authorization" (str "Bearer " api-key)})

(defn get-address [data] (-> (http/parse-payload data) :records :crypto.ETH.address))
(defn get-owner [data] (-> (http/parse-payload data) :meta :owner))
(defn get-tlds [data] (-> (http/parse-payload data) :tlds))

(defn domain-format [domain] string/lower-case domain)
(defn uns-resolve-get [name] (str uns-resolve-api (domain-format name)))
(defn valid-tld [domain] (async-storage/get-item :uns-tlds (fn [tlds] (let [domain-split (string/split domain ".")
                                                                            tld-count (count domain-split)
                                                                            tld (get domain-split (- tld-count 1))] (contains? tlds tld)))))

(defn is-valid-uns-name?
  [ens-name]
  (and ens-name
       (string? ens-name)
       (valid-tld ens-name)))

(defn address
  [ens-name cb]
  {:pre [(is-valid-uns-name? ens-name)]}
  (http/get (uns-resolve-get ens-name)
            #(cb (get-address %1))
            #(cb "0x")
            {}
            header))

(defn owner
  [ens-name cb]
  (http/get (uns-resolve-get ens-name)
            #(cb (get-owner %1))
            #(cb "0x")
            {}
            header))

(defn resolve-tlds [cb]
  (http/get uns-tld-api
            #(cb (get-tlds %))))