(ns legacy.status-im.utils.deprecated-types
  {:deprecated true :superseded-by "utils.transforms"}
  (:refer-clojure :exclude [js->clj])
  (:require
    [cljs-bean.core :as clj-bean]))

;; NOTE(19/12/22 yqrashawn) this namespace has been moved to the utils.transforms namespace,
;; we keep this only for old (status 1.0) code, can be removed with old code later

(defn js->clj
  [data]
  (cljs.core/js->clj data :keywordize-keys true))

(defn clj->pretty-json
  [data spaces]
  (.stringify js/JSON (clj-bean/->js data) nil spaces))

(defn clj->json
  [data]
  (clj->pretty-json data 0))

(defn json->clj
  [json]
  (when-not (= json "undefined")
    (try
      (js->clj (.parse js/JSON json))
      (catch js/Error _
        (when (string? json) json)))))

