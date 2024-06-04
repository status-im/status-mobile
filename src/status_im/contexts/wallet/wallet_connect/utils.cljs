(ns status-im.contexts.wallet.wallet-connect.utils
  (:require [react-native.wallet-connect :refer [parse-uri]]))

(defn version-supported? [version]
  (= version 2))

(defn- current-timestamp []
  (quot (.getTime (js/Date.)) 1000))

(defn uri-expired? [expiry-timestamp]
  (> (current-timestamp) expiry-timestamp))

(defn valid-connection? [parsed-uri]
  (let [{:keys [topic version expiryTimestamp]} parsed-uri]
    (and (seq topic)
         (not (uri-expired? expiryTimestamp))
         (version-supported? version))))

(defn valid-uri?
  [s]
  (-> s
      parse-uri
      valid-connection?))

(comment
  ()
  (def uri
    "wc:cd5a66f40e766669519ebcee4de7356fdd4529639632a384eabf414c02752763@2?expiryTimestamp=1715942649&relay-protocol=irn&symKey=8effdb2fb5d9919ddb0cac972cc450efb846a099ae4c73cf47eeb0fafe439245")
  (parse-uri uri)
  (valid-connection? uri)

  (parse-uri "asf")

  (boolean
   (or
    (seq "cd5a66f40e766669519ebcee4de7356fdd4529639632a384eabf414c02752763")
    (not (uri-expired? 1715942649))

    (version-supported? 2)))
  (uri-expired? 1815942649))
