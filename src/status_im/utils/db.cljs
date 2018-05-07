(ns status-im.utils.db
  (:require [clojure.string :as string]
            [cljs.spec.alpha :as spec]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.core :as ethereum]))

(defn hex-string? [s]
  (let [s' (if (string/starts-with? s "0x")
             (subs s 2)
             s)]
    (boolean (re-matches #"(?i)[0-9a-f]+" s'))))

(defn valid-length? [identity]
  (let [length (count identity)]
    (and
     (hex-string? identity)
     (or
      (and (= 128 length) (not (string/includes? identity "0x")))
      (and (= 130 length) (string/starts-with? identity "0x"))
      (and (= 132 length) (string/starts-with? identity "0x04"))
      (ethereum/address? identity)))))

(spec/def :global/not-empty-string (spec/and string? not-empty))
(spec/def :global/public-key (spec/and :global/not-empty-string valid-length?))
(spec/def :global/address ethereum/address?)
