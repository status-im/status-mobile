(ns status-im.protocol.web3.signature
  (:require [taoensso.timbre :refer-macros [debug]]))

(def elliptic-ec (.-ec (js/require "elliptic")))
(def secp (new elliptic-ec "secp256k1"))

(defn- slice [s from count]
  (->> s
       (into [])
       (take (+ from count))
       (drop from)
       (apply str)))

(defn sign [{:keys [web3 address message callback private-key]}]
  (when address
    (let [to-sign         (->> (.toString message)
                               (.sha3 web3))
          unlock-callback (fn [error res]
                            (if res
                              (.sign (.-eth web3) address to-sign callback)
                              (callback :unlock-error nil)))]
      (.unlockAccount (.-personal web3) address "testpass" nil unlock-callback))))

(defn signature-valid? [web3 message public-key signature]
  (let [signed (->> (.toString message)
                    (.sha3 web3))
        r   (slice signature 2 64)
        s   (slice signature 66 64)
        pk  (slice public-key 2 130)
        key (.keyFromPublic secp pk "hex")
        v   (.verify key signed (js-obj "r" r "s" s))]
    (.log js/console :verification-result v))
  true)