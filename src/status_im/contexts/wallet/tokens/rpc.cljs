(ns status-im.contexts.wallet.tokens.rpc
  (:require [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [utils.transforms :as transforms]))

(defn fetch-market-values
  [symbols currency]
  (-> (rpc-events/call-async "wallet_fetchMarketValues" true symbols currency)
      (promesa/then transforms/js->clj)))

(defn fetch-details
  [symbols]
  (-> (rpc-events/call-async "wallet_fetchTokenDetails" true symbols)
      (promesa/then transforms/js->clj)))

(defn fetch-prices
  [symbols currencies]
  (-> (rpc-events/call-async "wallet_fetchPrices" true symbols currencies)
      (promesa/then transforms/js->clj)))
