(ns messenger.persistence.realm
  (:require [cljs.reader :refer [read-string]]
            [syng-im.utils.logging :as log]
            [messenger.utils.types :refer [to-string]]))

(set! js/Realm (js/require "realm"))

(def opts {:schema [{:name       "Contact"
                     :properties {:phone-number     "string"
                                  :whisper-identity "string"
                                  :name             "string"
                                  :photo-path       "string"}}
                    {:name       :kv-store
                     :primaryKey :key
                     :properties {:key   "string"
                                  :value "string"}}]})

(def realm (js/Realm. (clj->js opts)))

(def schema-by-name (->> (:schema opts)
                         (mapv (fn [{:keys [name] :as schema}]
                                 [name schema]))
                         (into {})))


(defn field-type [schema-name field]
  (get-in schema-by-name [schema-name :properties field]))

(defn write [f]
  (.write realm f))

(defn create
  ([schema-name obj]
   (create schema-name obj false))
  ([schema-name obj update?]
   (.create realm (to-string schema-name) (clj->js obj) update?)))

(defmulti to-query (fn [schema-name operator field value]
                     operator))

(defmethod to-query :eq [schema-name operator field value]
  (let [value (to-string value)
        query (str (name field) "=" (if (= "string" (field-type schema-name field))
                                      (str "\"" value "\"")
                                      value))
        ;_     (log/debug query)
        ]
    query))

(defn get-by-field [schema-name field value]
  (-> (.objects realm (name schema-name))
      (.filtered (to-query schema-name :eq field value))))

(defn single [result]
  (-> (aget result 0)))

(defn single-cljs [result]
  (-> (aget result 0)
      (js->clj :keywordize-keys true)))

(defn decode-value [{:keys [key value]}]
  (read-string value))

(comment
  (use 'figwheel-sidecar.repl-api)
  (cljs-repl)

  (def x (-> (get-by-field :kv-store :key :identity)
             (single)))

  (aget x 1)

  (write (fn []
           (.delete realm (-> (get-by-field :kv-store :key :identity)
                              (single)))))


  (log/info (.keys js/Object realm))
  (log/info (clj->js opts))

  (clj->js (clj->js {:a [{:b 123}]}))

  )