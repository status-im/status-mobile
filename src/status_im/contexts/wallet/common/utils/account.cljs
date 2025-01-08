(ns status-im.contexts.wallet.common.utils.account
  (:require [clojure.string :as string]))

(defn filter-operable
  [accounts]
  (filter #(and (:operable? %)
                (not (:watch-only? %)))
          accounts))

(defn to-eip155
  [address chain-id]
  (str chain-id ":" address))

(defn from-eip155
  [address]
  (-> address
      (string/split #":")
      last))

