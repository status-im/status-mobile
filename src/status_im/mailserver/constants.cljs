(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.constants)

(def one-day (* 24 3600))
(def seven-days (* 7 one-day))
(def max-gaps-range (* 30 one-day))
(def max-request-range one-day)
(def maximum-number-of-attempts 2)
(def request-timeout 30)
(def min-limit 100)
(def max-limit 2000)
(def backoff-interval-ms 3000)
(def default-limit max-limit)
(def connection-timeout
  "Time after which mailserver connection is considered to have failed"
  10000)
