(ns status-im.utils.hex
  (:require [clojure.string :as s]))

(defn normalize-hex [hex]
  (if (and hex (s/starts-with? hex "0x"))
    (subs hex 2)
    hex))
