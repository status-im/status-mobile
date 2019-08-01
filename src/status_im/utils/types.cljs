(ns status-im.utils.types
  (:require [cognitect.transit :as transit]))

(defn to-string [s]
  (if (keyword? s)
    (name s)
    s))

(defn clj->json [data]
  (.stringify js/JSON (clj->js data)))

(defn json->clj [json]
  (when-not (= json "undefined")
    (try
      (js->clj (.parse js/JSON json) :keywordize-keys true)
      (catch js/Error _ (when (string? json) json)))))

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))
