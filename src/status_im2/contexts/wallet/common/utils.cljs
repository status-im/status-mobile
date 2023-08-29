(ns status-im2.contexts.wallet.common.utils
  (:require [clojure.string :as string]))

(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))
