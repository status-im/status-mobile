(ns status-im.js-dependencies
  (:require-macros [status-im.utils.js-require :as js-require]))

(def Chance              (js-require/js-require "chance"))
(def emojis              (js-require/js-require "emojilib"))
(def phishing-detect     (js-require/js-require "eth-phishing-detect"))
(def homoglyph-finder    (js-require/js-require "homoglyph-finder"))
(def identicon-js        (js-require/js-require "identicon.js"))
(def Web3                (js-require/js-require "web3"))
(defn web3-prototype []  (.-prototype (Web3)))
(def text-encoding       (js-require/js-require "text-encoding"))
(def js-sha3             (js-require/js-require "js-sha3"))
(def web3-utils          (js-require/js-require "web3-utils"))
(def hi-base32           (js-require/js-require "hi-base32"))
