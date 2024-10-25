(ns status-im.contexts.wallet.tokens.rpc
  (:require [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [status-im.contexts.wallet.tokens.data :as tokens-data]
            [utils.transforms :as transforms]))

(defn fetch-token-list
  []
  (-> (rpc-events/call-async "wallet_getTokenList" true)
      (promesa/then transforms/js->clj)
      (promesa/then tokens-data/normalize-tokens)))

(defn fetch-market-values
  [symbols currency]
  (-> (rpc-events/call-async "wallet_fetchMarketValues" true symbols currency)
      (promesa/then transforms/js->clj)
      (promesa/then tokens-data/normalize-market-values)))

(defn fetch-details
  [symbols]
  (-> (rpc-events/call-async "wallet_fetchTokenDetails" true symbols)
      (promesa/then transforms/js->clj)
      (promesa/then tokens-data/normalize-token-details)))

(defn fetch-prices
  [symbols currencies]
  (-> (rpc-events/call-async "wallet_fetchPrices" true symbols currencies)
      (promesa/then transforms/js->clj)
      (promesa/then tokens-data/normalize-prices)))

(defn fetch-additional-token-data
  [symbols currency]
  (-> (promesa/all [(fetch-market-values symbols currency)
                    (fetch-details symbols)
                    (fetch-prices symbols [currency])])
      (promesa/then (fn [[market-values details prices]]
                      {:market-values market-values
                       :details       details
                       :prices        prices}))))
