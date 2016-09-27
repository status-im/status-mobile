(ns status-im.chat.constants)

(def input-height 54)
(def max-input-height 66)
(def min-input-height 22)
(def input-spacing-top 16)
(def input-spacing-bottom 16)

(def request-info-height 61)
(def response-height-normal 211)
(def minimum-suggestion-height (+ input-height request-info-height))
(def suggestions-header-height 22)
(def minimum-command-suggestions-height
  (+ input-height suggestions-header-height))
