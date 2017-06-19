(ns status-im.protocol.web3.utils
  (:require [cljs-time.core :refer [now]]
            [cljs-time.coerce :refer [to-long]]))

(def web3 (js/require "web3"))

(defn from-utf8 [s]
  (.fromUtf8 web3.prototype s))

(defn to-ascii [s]
  (.toAscii web3.prototype s))

(defn to-utf8 [s]
  (.toUtf8 web3.prototype (str s)))

(defn shh [web3]
  (.-shh web3))

(defn timestamp []
  (to-long (now)))

(def Chance (js/require "chance"))
(def chance (Chance.))

(defn id []
  (str (timestamp) "-" (.guid chance)))

