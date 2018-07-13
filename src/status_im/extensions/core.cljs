(ns status-im.extensions.core
  (:require [clojure.string :as string]))

(defn url->storage-details [s]
  (when s
    (let [[_ type id] (string/split s #".*[:/]([a-z]*)@(.*)")]
      [(keyword type) id])))
