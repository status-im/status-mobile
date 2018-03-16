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

(defn- gen-price-url [fsyms tsyms]
  (str api-url "/pricemultifull?fsyms=" fsyms "&tsyms=" tsyms "&" status-identifier))

(defn- format-price-resp [from to resp]
  (let [raw (:RAW (types/json->clj resp))
        entry (get-in raw [(keyword from) (keyword to)])]
    {:from         from
     :to           to
     :price        (:PRICE entry)
     :last-day     (:OPEN24HOUR entry)}))

(defn get-prices [from to on-success on-error]
  (http/get
   (gen-price-url from to)
   (fn [resp] (on-success (format-price-resp from to resp)))
   on-error))
