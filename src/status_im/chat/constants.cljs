(ns status-im.chat.constants)

(def command-char "/")
(def spacing-char " ")
(def arg-wrapping-char "\"")

(def input-height 56)
(def max-input-height 66)
(def input-spacing-top 16)

(def crazy-math-message-id "crazy-math-message")
(def move-to-internal-failure-message-id "move-to-internal-failure-message")
(def passphrase-message-id "passphraze-message")
(def signing-phrase-message-id "signing-phrase-message")
(def intro-status-message-id "intro-status")
(def intro-message1-id "intro-message1")

;; TODO(janherich): figure out something better then this
(def send-command-ref ["transactor" :command 83 "send"])
(def request-command-ref ["transactor" :command 83 "request"])
(def phone-command-ref ["console" :command 50 "phone"])
