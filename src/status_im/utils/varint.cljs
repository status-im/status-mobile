(ns ^{:doc "Implementation of varint based on https://github.com/chrisdickinson/varint"}
 status-im.utils.varint
  (:require [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.js-dependencies :as dependencies]))

(def utils dependencies/web3-utils)

(def most-significant-bit 0x80)
(def biggest-int-per-byte 0x7F)
(def biggest-int 2147483648) ;; 2^31

(defn encode [num]
  (loop [num num
         out []]
    (if (>= num 128)
      (recur (if (>= num biggest-int)
               (/ num 128)
               (bit-shift-right num 7))
             (conj out (bit-or (bit-and num 0xFF)
                               most-significant-bit)))
      (conj out (bit-or num 0)))))

(defn encode-hex [num]
  (reduce (fn [hex current-bit]
            (str hex
                 (.leftPad utils
                           (abi-spec/number-to-hex current-bit)
                           2)))
          ""
          (encode num)))

(defn add-b-to-res
  [res b shift]
  (+ res (if (< shift 28)
           (bit-shift-left (bit-and b biggest-int-per-byte)
                           shift)
           (* (bit-and b biggest-int-per-byte)
              shift shift))))

(defn decode [buf]
  (loop [res 0
         shift 0
         [b & rest-buf] buf]
    (if (>= b most-significant-bit)
      (recur (add-b-to-res res b shift)
             (+ shift 7)
             rest-buf)
      (add-b-to-res res b shift))))

(defn decode-hex [hex]
  (->> hex
       (partition 2)
       (mapv #(abi-spec/hex-to-number (apply str %)))
       decode))
