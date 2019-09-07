(ns status-im.utils.types
  (:refer-clojure :exclude [js->clj])
  (:require [cljs-bean.core :as clj-bean]))

(defn to-string [s]
  (if (keyword? s)
    (name s)
    s))

(defn js->clj [data]
  (cljs.core/js->clj data :keywordize-keys true))

(defn clj->json [data]
  (.stringify js/JSON (clj-bean/->js data)))

(defn json->clj [json]
  (when-not (= json "undefined")
    (try
      (js->clj (.parse js/JSON json))
      (catch js/Error _ (when (string? json) json)))))

(def serialize clj->json)
(defn deserialize [o] (try (json->clj o)
                           (catch :default _ nil)))
