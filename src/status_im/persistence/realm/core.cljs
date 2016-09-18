(ns status-im.persistence.realm.core
  (:require [cljs.reader :refer [read-string]]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.utils.types :refer [to-string]]
            [status-im.utils.utils :as u]
            [status-im.utils.fs :as fs]
            [taoensso.timbre :as log]
            [status-im.persistence.realm.schemas :refer [base account]]
            [clojure.string :as str])
  (:refer-clojure :exclude [exists?]))

(def new-account-filename "new-account")

(def realm-class (u/require "realm"))

(defn create-account-realm [address]
  (let [opts (merge account {:path (str address ".realm")})]
    (when (cljs.core/exists? js/window)
      (realm-class. (clj->js opts)))))

(def base-realm 
  (when (cljs.core/exists? js/window)
    (realm-class. (clj->js base))))

(def account-realm (atom (create-account-realm new-account-filename)))

(defn is-new-account? []
  (let [path (.-path @account-realm)
        realm_file (str new-account-filename ".realm")]
    (str/ends-with? path realm_file)))

(defn realm [schema]
  (case schema
    :base base-realm
    :account @account-realm))

(defn close [schema]
  (let [realm-db (realm schema)]
    (when realm-db
        (.close realm-db))))

(defn close-account-realm []
  (close :account)
  (reset! account-realm nil))

(defn reset-account []
  (when @account-realm
    (close-account-realm))
  (reset! account-realm (create-account-realm new-account-filename))
  (.write @account-realm #(.deleteAll @account-realm)))

(defn move-file-handler [address err handler]
  (log/debug "Moved file with error: " err address)
  (if err
    (log/error "Error moving account realm: " (.-message err))
    (reset! account-realm (create-account-realm address)))
  (handler err))

(defn change-account-realm [address new-account? handler]
  (let [path (.-path @account-realm)
        realm-file (str new-account-filename ".realm")]
    (log/debug "closing account realm: " path)
    (close-account-realm)
    (log/debug "is new account? " new-account?)
    (if new-account?
      (let [new-path (str/replace path realm-file (str address ".realm"))]
        (log/debug "Moving file to " new-path)
        (fs/move-file path new-path #(move-file-handler address % handler)))
      (do
        (reset! account-realm (create-account-realm address))
        (handler nil)))))

(defn get-schema-by-name [opts]
  (->> (:schema opts)
       (mapv (fn [{:keys [name] :as schema}]
               [name schema]))
       (into {})))

(def schema-by-name 
  {:base (get-schema-by-name base)
   :account (get-schema-by-name account)})

(defn field-type [schema schema-name field]
  (let [schema-by-name (get schema-by-name schema)
        field-def (get-in schema-by-name [schema-name :properties field])]
    (if (map? field-def)
      (:type field-def)
      field-def)))

(defn write [schema f]
  (.write (realm schema) f))

(defn create
  ([schema schema-name obj]
   (create schema schema-name obj false))
  ([schema schema-name obj update?]
   (.create (realm schema) (to-string schema-name) (clj->js obj) update?)))

(defn create-object
  [schema schema-name obj]
  (write schema (fn [] (create schema schema-name obj true))))

(defn and-query [queries]
  (str/join " and " queries))

(defn or-query [queries]
  (str/join " or " queries))

(defmulti to-query (fn [schema schema-name operator field value]
                     operator))

(defmethod to-query :eq [schema schema-name operator field value]
  (let [value (to-string value)
        field-type (field-type schema schema-name field)
        query (str (name field) "=" (if (= "string" (name field-type))
                                      (str "\"" value "\"")
                                      value))]
    query))

(defn get-by-filter [schema schema-name filter]
  (-> (.objects (realm schema) (name schema-name))
      (.filtered filter)))

(defn get-by-field [schema schema-name field value]
  (let [q (to-query schema schema-name :eq field value)]
    (.filtered (.objects (realm schema) (name schema-name)) q)))

(defn get-by-fields [schema schema-name op fields]
  (let [queries (map (fn [[k v]]
                       (to-query schema schema-name :eq k v))
                     fields)]
    (.filtered (.objects (realm schema) (name schema-name))
               (case op
                 :and (and-query queries)
                 :or (or-query queries)))))

(defn get-all [schema schema-name]
  (.objects (realm schema) (to-string schema-name)))

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

(defn delete [schema obj]
  (.delete (realm schema) obj))

(defn exists? [schema schema-name fields]
  (pos? (.-length (get-by-fields schema schema-name :and fields))))

(defn get-count [objs]
  (.-length objs))

(defn get-list [schema schema-name]
  (vals (js->clj (.objects (realm schema) (to-string schema-name)) :keywordize-keys true)))

(defn realm-collection->list [collection]
  (-> (.map collection (fn [object _ _] object))
      (js->clj :keywordize-keys true)))

(defn get-one-by-field [schema schema-name field value]
  (single-cljs (get-by-field schema schema-name field value)))
