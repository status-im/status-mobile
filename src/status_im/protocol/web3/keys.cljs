(ns status-im.protocol.web3.keys
  (:require [taoensso.timbre :as log]))

(defn new-key-pair [web3 callback]
  (.. web3
      -shh
      (newKeyPair callback)))

(def status-key-password "status-key-password")
(def status-group-key-password "status-public-group-key-password")

(defonce password->keys (atom {}))

(defn- add-sym-key-from-password
  [web3 password callback]
  (.. web3
      -shh
      (generateSymKeyFromPassword password callback)))

(defn get-sym-key
  "Memoizes expensive calls by password."
  ([web3 password success-fn]
   ;; TODO:(dmitryn) add proper error handling
   ;; to other usages of get-sym-key fn
   (get-sym-key web3 password success-fn #(log/error %)))
  ([web3 password success-fn error-fn]
   (if-let [key-id (get @password->keys password)]
     (success-fn key-id)
     (add-sym-key-from-password
      web3 password
      (fn [err res]
        (if err
          (error-fn err)
          (do (swap! password->keys assoc password res)
              (success-fn res))))))))

(defn reset-keys! []
  (reset! password->keys {}))
