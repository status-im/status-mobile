(ns status-im.utils.eip.eip67
  "Utility function related to [EIP67](https://github.com/ethereum/EIPs/issues/67)"
  (:require [clojure.string :as string]))

(def scheme "ethereum")
(def scheme-separator ":")
(def parameters-separator "?")
(def parameter-separator "&")
(def key-value-separator "=")

(def key-value-format (str "([^" parameter-separator key-value-separator "]+)"))
(def parameters-pattern (re-pattern (str key-value-format key-value-separator key-value-format)))

(defn- parse-parameters [s]
  (when s
    (into {} (for [[_ k v] (re-seq parameters-pattern s)]
               [(keyword k) v]))))

(defn parse-uri
  "Parse a EIP 67 URI as a map of keyword / strings. Parsed map will contain at least the key `address`.
  Invalid URI will be parsed as `nil`."
  [s]
  (when (and s (string/starts-with? s scheme))
    (let [[address parameters] (string/split (string/replace s (str scheme scheme-separator) "") parameters-separator)]
      (when-not (zero? (count address))
        (merge
          {:address address}
          (parse-parameters parameters))))))

(defn- generate-parameter-string [m]
  (string/join parameter-separator (for [[k v] m]
                                     (str (name k) key-value-separator  v))))

(defn generate-uri
  "Generate a EIP 67 URI based on `address` and an optional map of extra properties.
   No validation of address format is performed."
  ([address] (generate-uri address nil))
  ([address m]
   (when address
     (str scheme scheme-separator address (when m (str parameters-separator (generate-parameter-string m)))))))