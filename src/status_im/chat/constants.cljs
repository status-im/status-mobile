(ns status-im.chat.constants)

(def command-char "/")
(def spacing-char " ")
(def arg-wrapping-char "\"")

(def spam-message-frequency-threshold 4)
(def spam-interval-ms 1000)
(def default-cooldown-period-ms 10000)
(def cooldown-reset-threshold 3)
(def cooldown-periods-ms
  {1 2000
   2 5000
   3 10000})

(def max-text-size 4096)
