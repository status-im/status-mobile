(ns syng-im.persistence.realm
  (:require [cljs.reader :refer [read-string]]
            [syng-im.utils.types :refer [to-string]])
  (:refer-clojure :exclude [exists?]))

(set! js/window.Realm (js/require "realm"))

(def opts {:schema [{:name       :contacts
                     :primaryKey :whisper-identity
                     :properties {:phone-number     {:type     "string"
                                                     :optional true}
                                  :whisper-identity "string"
                                  :name             {:type     "string"
                                                     :optional true}
                                  :photo-path       {:type    "string"
                                                     :optinal true}}}
                    {:name       :kv-store
                     :primaryKey :key
                     :properties {:key   "string"
                                  :value "string"}}
                    {:name       :msgs
                     :primaryKey :msg-id
                     :properties {:msg-id          "string"
                                  :from            "string"
                                  :to              {:type     "string"
                                                    :optional true}
                                  :content         "string" ;; TODO make it ArrayBuffer
                                  :content-type    "string"
                                  :timestamp       "int"
                                  :chat-id         {:type    "string"
                                                    :indexed true}
                                  :outgoing        "bool"
                                  :delivery-status {:type     "string"
                                                    :optional true}}}
                    {:name       :chat-contact
                     :properties {:identity         "string"
                                  :text-color       "string"
                                  :background-color "string"}}
                    {:name       :chats
                     :primaryKey :chat-id
                     :properties {:chat-id    "string"
                                  :name       "string"
                                  :group-chat {:type    "bool"
                                               :indexed true}
                                  :is-active  "bool"
                                  :timestamp  "int"
                                  :contacts   {:type       "list"
                                               :objectType "chat-contact"}}}
                    {:name        :tag
                     :primaryKey  :name
                     :properties  {:name         "string"
                                   :count        {:type     "int"
                                                  :optional true
                                                  :default 0}}}
                    {:name        :discoveries
                     :primaryKey  :whisper-id
                     :properties  {:name         "string"
                                   :status       "string"
                                   :whisper-id   "string"
                                   :photo        "string"
                                   :location     "string"
                                   :tags         {:type       "list"
                                                  :objectType "tag"}
                                   :last-updated "date"}}

                    ]})


(def realm (js/Realm. (clj->js opts)))

(def schema-by-name (->> (:schema opts)
                         (mapv (fn [{:keys [name] :as schema}]
                                 [name schema]))
                         (into {})))

(defn field-type [schema-name field]
  (let [field-def (get-in schema-by-name [schema-name :properties field])]
    (if (map? field-def)
      (:type field-def)
      field-def)))

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
                                      value))]
    query))

(defn get-by-filter [schema-name filter]
  (-> (.objects realm (name schema-name))
      (.filtered filter)))

(defn get-by-field [schema-name field value]
  (let [q (to-query schema-name :eq field value)]
    (-> (.objects realm (name schema-name))
        (.filtered q))))

(defn get-all [schema-name]
  (.objects realm (to-string schema-name)))

(defn sorted [results field-name order]
  (.sorted results (to-string field-name) (if (= order :asc)
                                            false
                                            true)))

(defn filtered [results filter-query]
  (.filtered results filter-query))

(defn page [results from to]
  (js/Array.prototype.slice.call results from to))

(defn single [result]
  (-> (aget result 0)))

(defn single-cljs [result]
  (some-> (aget result 0)
          (js->clj :keywordize-keys true)))

(defn list-to-array [record list-field]
  (assoc record list-field (-> (get record list-field)
                               vals
                               vec)))

(defn decode-value [{:keys [key value]}]
  (read-string value))

(defn delete [obj]
  (.delete realm obj))

(defn exists? [schema-name field value]
  (> (.-length (get-by-field schema-name field value))
     0))

(defn get-count [objs]
  (.-length objs))

(defn get-list [schema-name]
  (vals (js->clj (.slice (.objects realm (to-string schema-name)) 0) :keywordize-keys true)))


(comment

  )