(ns legacy.status-im.utils.prices
  (:require
    [status-im2.common.json-rpc.events :as json-rpc]))

(defn- format-price-resp
  [resp to mainnet?]
  (if mainnet?
    resp
    {:ETH (into {}
                (for [[_ price] resp]
                  {(keyword to) price}))}))

(defn get-prices
  [from to mainnet? on-success on-error]
  (json-rpc/call {:method     "wallet_fetchPrices"
                  :params     [from to]
                  :on-success (fn [resp] (on-success (format-price-resp resp to mainnet?)))
                  :on-error   on-error}))
