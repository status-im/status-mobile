(ns status-im.utils.types)

(defn to-string [s]
  (if (keyword? s)
    (name s)
    s))

(defn to-edn-string [value]
  (with-out-str (pr value)))

(defn clj->json [data]
  (.stringify js/JSON (clj->js data)))

(defn json->clj [data]
  (js->clj (.parse js/JSON data) :keywordize-keys true))
