(ns status-im.protocol.web3.utils
  (:require [cljs-time.core :refer [now]]
            [cljs-time.coerce :refer [to-long]]
            [status-im.utils.web3-provider :as w3]
            [status-im.js-dependencies :as dependencies]))

(defn from-utf8 [s]
  (.fromUtf8 dependencies/Web3.prototype s))

(defn to-ascii [s]
  (.toAscii dependencies/Web3.prototype s))

(defn to-utf8 [s]
  (.toUtf8 dependencies/Web3.prototype (str s)))

(defn shh [web3]
  (.-shh web3))

(defn timestamp []
  (to-long (now)))
