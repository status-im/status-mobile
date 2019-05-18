(ns status-im.js-dependencies
  (:require-macros [status-im.utils.js-require :as require]))

(def Chance              (require/js-require "chance"))
(def emojis              (require/js-require "emojilib"))
(def phishing-detect     (require/js-require "eth-phishing-detect"))
(def homoglyph-finder    (require/js-require "homoglyph-finder"))
(def identicon-js        (require/js-require "identicon.js"))
(def Web3                (js/require "web3"))
(def text-encoding       (require/js-require "text-encoding"))
(def js-sha3             (require/js-require "js-sha3"))
(def web3-utils          (require/js-require "web3-utils"))
(def hi-base32           (require/js-require "hi-base32"))
