(ns status-im.chat.constants)

(def command-char "/")
(def bot-char "@")

(def input-height 56)
(def max-input-height 66)
(def min-input-height 22)
(def input-spacing-top 16)
(def input-spacing-bottom 18)

(def request-info-height 61)
(def response-height-normal 211)
(def minimum-suggestion-height (+ input-height request-info-height))
(def suggestions-header-height 22)
(def minimum-command-suggestions-height
  (+ input-height suggestions-header-height))

(def emoji-container-height 250)

(def crazy-math-message-id "crazy-math-message")
(def passphrase-message-id "passphraze-message")
(def intro-status-message-id "intro-status")
(def intro-message1-id "intro-message1")
