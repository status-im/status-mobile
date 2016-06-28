(ns status-im.persistence.realm
  (:require [cljs.reader :refer [read-string]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.utils.types :refer [to-string]]
            [status-im.utils.utils :as u])
  (:refer-clojure :exclude [exists?]))

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
                                                    :optional true}
                                  :same-author     "bool"
                                  :same-direction  "bool"
                                  :preview         {:type     :string
                                                    :optional true}}}
                    {:name       :chat-contact
                     :properties {:identity   "string"
                                  :is-in-chat {:type    "bool"
                                               :default true}}}
                    {:name       :chats
                     :primaryKey :chat-id
                     :properties {:chat-id     "string"
                                  :name        "string"
                                  :color       {:type    "string"
                                                :default default-chat-color}
                                  :group-chat  {:type    "bool"
                                                :indexed true}
                                  :is-active   "bool"
                                  :timestamp   "int"
                                  :contacts    {:type       "list"
                                                :objectType "chat-contact"}
                                  :dapp-url    {:type     :string
                                                :optional true}
                                  :dapp-hash   {:type     :int
                                                :optional true}
                                  :last-msg-id "string"}}
                    {:name       :commands
                     :primaryKey :chat-id
                     :properties {:chat-id "string"
                                  :file    "string"}}
                    {:name       :tag
                     :primaryKey :name
                     :properties {:name  "string"
                                  :count {:type     "int"
                                          :optional true
                                          :default  0}}}
                    {:name       :discoveries
                     :primaryKey :whisper-id
                     :properties {:name         "string"
                                  :status       "string"
                                  :whisper-id   "string"
                                  :photo        "string"
                                  :location     "string"
                                  :tags         {:type       "list"
                                                 :objectType "tag"}
                                  :last-updated "date"}}]})

(def realm-class (u/require "realm"))

(def realm (when (cljs.core/exists? js/window)
             (realm-class. (clj->js opts))))

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

(defn create-object
  [schema-name obj]
  (write (fn [] (create schema-name obj true))))

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
    (.filtered (.objects realm (name schema-name)) q)))

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

(defn cljs-list [results]
  (-> (js->clj results :keywordize-keys true)
      (vals)))

(defn list-to-array [record list-field]
  (update-in record [list-field] (comp vec vals)))

(defn decode-value [{:keys [key value]}]
  (read-string value))

(defn delete [obj]
  (.delete realm obj))

(defn exists? [schema-name field value]
  (pos? (.-length (get-by-field schema-name field value))))

(defn get-count [objs]
  (.-length objs))

(defn get-list [schema-name]
  (vals (js->clj (.objects realm (to-string schema-name)) :keywordize-keys true)))

(defn collection->map [collection]
  (-> (.map collection (fn [object _ _] object))
      (js->clj :keywordize-keys true)))

(defn get-one-by-field [schema-name field value]
  (single-cljs (get-by-field schema-name field value)))
