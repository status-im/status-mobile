(ns legacy.status-im.utils.hex
  (:require
    [clojure.string :as string]))

(defn normalize-hex
  [hex]
  (when hex
    (string/lower-case (if (string/starts-with? hex "0x")
                         (subs hex 2)
                         hex))))

(defn valid-hex?
  [hex]
  (let [hex (normalize-hex hex)]
    (and (re-matches #"^[0-9a-fA-F]+$" hex)
         (not= (js/parseInt hex 16) 0))))
