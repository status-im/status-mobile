(ns status-im.async-storage.transit
  (:require [cognitect.transit :as transit]
            [taoensso.timbre :as timbre]))

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn clj->transit [o] (transit/write writer o))
(defn transit->clj [o] (try (transit/read reader o)
                            (catch :default e
                              (timbre/error e))))
