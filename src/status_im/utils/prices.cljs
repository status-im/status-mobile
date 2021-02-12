(ns status-im.utils.prices
  (:require [clojure.string :as string]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

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
  ((comp (partial string/join ",") (partial map name)) syms))

(defn- gen-price-url [fsyms tsyms]
  (str api-url "/pricemultifull?fsyms=" (->url-param-syms fsyms) "&tsyms=" (->url-param-syms tsyms) "&" status-identifier))

(defn- format-price-resp [resp mainnet?]
  ;;NOTE(this check is to allow value conversion for sidechains with native currencies listed on cryptocompare
  ;; under a symbol different than display symbol. Specific use case xDAI and POA.
  (if mainnet?
    (into {} (for [[from entries] (:RAW (types/json->clj resp))]
               {from (into {} (for [[to entry] entries]
                                {to {:from     (name from)
                                     :to       (name to)
                                     :price    (:PRICE entry)
                                     :last-day (:OPEN24HOUR entry)}}))}))
    (into {} (for [[_ entries] (:RAW (types/json->clj resp))]
               {:ETH (into {} (for [[to entry] entries]
                                {to {:from     "ETH"
                                     :to       (name to)
                                     :price    (:PRICE entry)
                                     :last-day (:OPEN24HOUR entry)}}))}))))

(defn get-prices [from to mainnet? on-success on-error]
  (log/debug "[prices] get-prices"
             "from" from
             "to" to
             "mainnet?" mainnet?)
  (http/get
   (gen-price-url from to)
   (fn [resp] (on-success (format-price-resp resp mainnet?)))
   on-error))
