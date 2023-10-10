(ns status-im2.contexts.syncing.utils
  (:require [clojure.string :as string]
            [status-im2.constants :as constants]))

(defn valid-connection-string?
  [connection-string]
  (when connection-string
    (string/starts-with?
     connection-string
     constants/local-pairing-connection-string-identifier)))
