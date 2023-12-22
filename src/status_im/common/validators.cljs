(ns status-im.common.validators
  (:require
    [status-im.constants :as constants]))

(defn valid-public-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches constants/regx-public-key s))))

(defn valid-compressed-key?
  [s]
  (and (string? s)
       (not-empty s)
       (boolean (re-matches constants/regx-compressed-key s))))
