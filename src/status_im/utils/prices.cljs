(ns status-im.utils.prices
  (:require [status-im.utils.http :as http]
            [status-im.utils.types :as types]))

;; Responsible for interacting with Cryptocompare API to get current prices for
;; currencies and tokens.
;;
;; No tests since fetch API (via http-get) relies on `window` being available.
;;
;; Example usage:
;; (get-prices "ETH" "USD" println print)

(def api-url "https://min-api.cryptocompare.com/data")
(def status-identifier "extraParams=Status.im")

(defn- ->url-param-syms [syms]
  ((comp (partial clojure.string/join ",") (partial map name)) syms))

(defn- gen-price-url [fsyms tsyms]
  (str api-url "/pricemultifull?fsyms=" (->url-param-syms fsyms) "&tsyms=" (->url-param-syms tsyms) "&" status-identifier))

(defn- format-price-resp [resp]
  (into {} (for [[from entries] (:RAW (types/json->clj resp))]
             {from (into {} (for [[to entry] entries]
                              {to {:from     (name from)
                                   :to       (name to)
                                   :price    (:PRICE entry)
                                   :last-day (:OPEN24HOUR entry)}}))})))

(defn get-prices [from to on-success on-error]
  (http/get
   (gen-price-url from to)
   (fn [resp] (on-success (format-price-resp resp)))
   on-error))
