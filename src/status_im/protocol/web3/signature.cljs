(ns status-im.protocol.web3.signature
  (:require [taoensso.timbre :refer-macros [debug]]))

(def elliptic-ec (.-ec (js/require "elliptic")))
(def secp (new elliptic-ec "secp256k1"))

(defn sign [{:keys [web3 address message callback]}]
  (when address
    (let [to-sign         (->> (.toString message)
                               (.sha3 web3))
          unlock-callback (fn [error res]
                            (if res
                              (.sign (.-eth web3) address to-sign callback)
                              (callback :unlock-error nil)))]
      (.unlockAccount (.-personal web3) address nil nil unlock-callback))))

(defn signature-valid? [web3 message public-key signature]
  (let [message   (->> (.toString message)
                       (.sha3 web3))
        signature (.substr signature 2)
        r         (.substr signature 0 64)
        s         (.substr signature 64 64)
        v         (.toHex web3 (- (.toDecimal web3 (str "0x" (.substr signature 128 2))) 27))
        key       (.keyFromPublic secp (.substr public-key 2) "hex")
        result    (.verify key message (js-obj "r" r "s" s "v" v))])
  result)