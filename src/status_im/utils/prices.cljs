(ns status-im.utils.prices
  (:require [status-im2.common.json-rpc.events :as json-rpc]))

(defn- format-price-resp
  [resp to mainnet?]
  (if mainnet?
    (when (seq resp)
      (into {}
            (for [[from price] resp]
              {from {(keyword to) {:from  (name from)
                                   :to    to
                                   :price price}}})))
    {:ETH (into {}
                (for [[_ price] resp]
                  {(keyword to) {:from  "ETH"
                                 :to    to
                                 :price price}}))}))

(defn get-prices
  [from to mainnet? on-success on-error]
  (let [to (first to)]
    (json-rpc/call {:method     "wallet_fetchPrices"
                    :params     [from to]
                    :on-success (fn [resp] (on-success (format-price-resp resp to mainnet?)))
                    :on-error   on-error})))