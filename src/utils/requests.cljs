(ns utils.requests
  (:require [utils.datetime :as datetime]))

(def request-cooldown-ms (* 24 60 60 1000))

(defn can-request-access-again?
  [requested-at]
  (> (datetime/timestamp) (+ (* requested-at 1000) request-cooldown-ms)))

