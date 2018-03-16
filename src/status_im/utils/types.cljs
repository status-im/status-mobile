(ns status-im.utils.types)

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
