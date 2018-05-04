(ns status-im.chat.constants)

(def command-char "/")
(def spacing-char " ")
(def arg-wrapping-char "\"")

(def input-height 56)
(def input-spacing-top 16)

(def console-chat-id "console")

;; TODO(janherich): figure out something better then this
(def send-command-ref ["transactor" :command 83 "send"])
(def request-command-ref ["transactor" :command 83 "request"])
