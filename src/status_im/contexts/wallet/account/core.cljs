(ns status-im.contexts.wallet.account.core
  (:require [clojure.string :as string]))

(defn get-token
  [account token-symbol]
  (->> account
       :tokens
       (filter #(= (string/lower-case (:symbol %))
                   (string/lower-case
                    token-symbol)))
       first))
