(ns status-im.contexts.wallet.wallet-connect.utils
  (:require [react-native.wallet-connect :refer [parse-uri]]))

(defn version-supported?
  [version]
  (= version 2))

(defn- current-timestamp
  []
  (quot (.getTime (js/Date.)) 1000))

(defn timestamp-expired?
  [expiry-timestamp]
  (> (current-timestamp) expiry-timestamp))

(defn valid-wc-uri?
  [parsed-uri]
  (let [{:keys [topic version expiryTimestamp]} parsed-uri]
    (and (seq topic)
         (number? version)
         (number? expiryTimestamp))))

(defn valid-uri?
  "Check if the uri is in the wallet-connect format.
  At this stage, the uri might be expired or from an unsupported version"
  [s]
  (-> s
      parse-uri
      valid-wc-uri?))
