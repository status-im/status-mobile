(ns status-im.data-store.realm.core
  (:require [status-im.utils.utils :as u]
            [status-im.utils.types :refer [to-string]]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [clojure.string :as str])
  (:refer-clojure :exclude [exists?]))

(def realm-class (u/require "realm"))

(defn realm-version
  [file-name]
  (.schemaVersion realm-class file-name))

(defn open-realm
  [options file-name]
  (let [options (merge options {:path file-name})]
    (when (cljs.core/exists? js/window)
      (realm-class. (clj->js options)))))

(defn close [realm]
  (.close realm))

(defn migrate [file-name schemas]
  (let [current-version (realm-version file-name)]
    (doseq [schema schemas
            :when (> (:schemaVersion schema) current-version)
            :let [migrated-realm (open-realm schema file-name)]]
      (close migrated-realm))))

(defn open-migrated-realm
  [file-name schemas]
  (migrate file-name schemas)
  (open-realm (last schemas) file-name))

(def new-account-filename "new-account")
(def new-accout-realm-file (str new-account-filename ".realm"))

(def base-realm (open-migrated-realm (.-defaultPath realm-class) base/schemas))

(def account-realm (atom (open-migrated-realm new-account-filename account/schemas)))

(defn close-account-realm []
  (close @account-realm)
  (reset! account-realm nil))

(defn reset-account []
  (when @account-realm
    (close @account-realm))
  (reset! account-realm (open-migrated-realm new-account-filename account/schemas))
  (.write @account-realm #(.deleteAll @account-realm)))

(defn move-file-handler [address err handler]
  (log/debug "Moved file with error: " err address)
  (if err
    (log/error "Error moving account realm: " (.-message err))
    (reset! account-realm (open-migrated-realm address account/schemas)))
  (handler err))

(defn change-account [address new-account? handler]
  (let [path (.-path @account-realm)]
    (log/debug "closing account realm: " path)
    (close-account-realm)
    (log/debug "is new account? " new-account?)
    (if new-account?
      (let [new-path (str/replace path new-accout-realm-file (str address ".realm"))]
        (log/debug "Moving file to " new-path)
        (fs/move-file path new-path #(move-file-handler address % handler)))
      (do
        (reset! account-realm (open-migrated-realm address account/schemas))
        (handler nil)))))

; realm functions

(defn and-query [queries]
  (str/join " and " queries))

(defn or-query [queries]
  (str/join " or " queries))

(defn write [realm f]
  (.write realm f))

(defn create
  ([realm schema-name obj]
   (create realm schema-name obj false))
  ([realm schema-name obj update?]
   (.create realm (to-string schema-name) (clj->js obj) update?)))

(defn save
  ([realm schema-name obj]
   (save realm schema-name obj false))
  ([realm schema-name obj update?]
   (write realm #(create realm schema-name obj update?))))

(defn save-all
  ([realm schema-name objs]
   (save-all realm schema-name objs false))
  ([realm schema-name objs update?]
   (write realm (fn []
                  (mapv #(save realm schema-name % update?) objs)))))

(defn delete [realm obj]
  (write realm #(.delete realm obj)))

(defn get-all [realm schema-name]
  (.objects realm (to-string schema-name)))

(defn sorted [results field-name order]
  (.sorted results (to-string field-name) (if (= order :asc)
                                            false
                                            true)))

(defn get-count [objs]
  (.-length objs))

(defn page [results from to]
  (js/Array.prototype.slice.call results from to))

(defn filtered [results filter-query]
  (.filtered results filter-query))

(defn realm-collection->list [collection]
  (-> (.map collection (fn [object _ _] object))
      (js->clj :keywordize-keys true)))

(defn list->array [record list-field]
  (update-in record [list-field] (comp vec vals)))

(defn single [result]
  (-> (aget result 0)))

(defn single-cljs [result]
  (some-> (aget result 0)
          (js->clj :keywordize-keys true)))

(defn get-by-filter [realm schema-name filter]
  (-> (.objects realm (name schema-name))
      (.filtered filter)))

(defn- get-schema-by-name [opts]
  (->> opts
       (mapv (fn [{:keys [name] :as schema}]
               [(keyword name) schema]))
       (into {})))

(defn- field-type [realm schema-name field]
  (let [schema-by-name (get-schema-by-name (js->clj (.-schema realm) :keywordize-keys true))
        field-def (get-in schema-by-name [schema-name :properties field])]
    (if (map? field-def)
      (:type field-def)
      field-def)))

(defmulti to-query (fn [realm schema-name operator field value]
                     operator))

(defmethod to-query :eq [realm schema-name operator field value]
  (let [value (to-string value)
        field-type (field-type realm schema-name field)
        query (str (name field) "=" (if (= "string" (name field-type))
                                      (str "\"" value "\"")
                                      value))]
    query))

(defn get-by-field [realm schema-name field value]
  (let [q (to-query realm schema-name :eq field value)]
    (.filtered (.objects realm (name schema-name)) q)))

(defn get-one-by-field [realm schema-name field value]
  (single (get-by-field realm schema-name field value)))

(defn get-one-by-field-clj [realm schema-name field value]
  (single-cljs (get-by-field realm schema-name field value)))

(defn get-by-fields [realm schema-name op fields]
  (let [queries (map (fn [[k v]]
                       (to-query realm schema-name :eq k v))
                     fields)]
    (.filtered (.objects realm (name schema-name))
               (case op
                 :and (and-query queries)
                 :or (or-query queries)))))

(defn exists? [realm schema-name fields]
  (pos? (.-length (get-by-fields realm schema-name :and fields))))