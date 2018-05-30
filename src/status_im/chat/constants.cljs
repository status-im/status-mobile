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

(def spam-message-frequency-threshold 4)
(def spam-interval-ms 1000)
(def default-cooldown-period-ms 10000)
(def cooldown-reset-threshold 3)
(def cooldown-periods-ms
  {1 2000
   2 5000
   3 10000})
