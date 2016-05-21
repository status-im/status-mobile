(ns status-im.persistence.realm-queries
  (:require [clojure.string :as s]
            [status-im.utils.types :refer [to-string]]))

(defn include-query [field-name values]
  (->> values
       (map (fn [val]
              (str (to-string field-name) " == " (if (string? val)
                                                   (str "'" val "'")
                                                   val))))
       (s/join " || ")))

(defn exclude-query [field-name values]
  (->> values
       (map (fn [val]
              (str (to-string field-name) " != " (if (string? val)
                                                   (str "'" val "'")
                                                   val))))
       (s/join " && ")))