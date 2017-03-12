(ns status-im.utils.hex
  (:require [clojure.string :as s]))

(defn normalize-hex [hex]
  (if (and hex (s/starts-with? hex "0x"))
    (subs hex 2)
    hex))

(defn valid-hex? [hex]
  (let [hex (normalize-hex hex)]
    (and (re-matches #"^[0-9a-fA-F]+$" hex)
         (not= (js/parseInt hex 16) 0))))