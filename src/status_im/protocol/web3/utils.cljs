(ns status-im.protocol.web3.utils
  (:require [cljs-time.coerce :refer [to-long]]
            [cljs-time.core :refer [now]]
            [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

(defn from-utf8 [s]
  (.fromUtf8 dependencies/Web3.prototype s))

(defn to-ascii [s]
  (.toAscii dependencies/Web3.prototype s))

(defn to-utf8 [s]
  (.toUtf8 dependencies/Web3.prototype (str s)))

(defn shh [web3]
  (.-shh web3))

(defn extract-enode-id [enode]
  (-> enode
      (string/split #"/")
      (get 2 "")
      (string/split #":")
      (get 0 "")
      (string/split "@")
      (get 0)))
