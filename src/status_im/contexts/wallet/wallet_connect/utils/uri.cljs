(ns status-im.contexts.wallet.wallet-connect.utils.uri
  (:require [react-native.wallet-connect :as wallet-connect]))

(defn version-supported?
  [version]
  (= version 2))

(defn- current-timestamp
  []
  (quot (.getTime (js/Date.)) 1000))

(defn timestamp-expired?
  [expiry-timestamp]
  (when expiry-timestamp
    (> (current-timestamp) expiry-timestamp)))

(defn valid-wc-uri?
  [parsed-uri]
  (let [{:keys [topic version]} parsed-uri]
    (and (seq topic)
         (number? version))))

(defn valid-uri?
  "Check if the uri is in the wallet-connect format.
  At this stage, the uri might be expired or from an unsupported version"
  [s]
  (-> s
      wallet-connect/parse-uri
      valid-wc-uri?))
