(ns status-im.data-store.realm.core
  (:require [status-im.utils.types :refer [to-string]]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [clojure.string :as str]
            [goog.string :as gstr])
  (:refer-clojure :exclude [exists?]))

(def realm-class (js/require "realm"))

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
      (let [new-path (str/replace path new-account-filename address)]
        (log/debug "Moving file " path " to " new-path)
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

(defn add-js->clj-array
  "Extends type with IEncodeClojure and treats it as js array."
  [t]
  (extend-type t
    IEncodeClojure
    (-js->clj
      ([x options]
       (vec (map #(apply clj->js % options) x))))))

(defn add-js->clj-object [t]
  "Extends type with IEncodeClojure and treats it as js object."
  (extend-type t
    IEncodeClojure
    (-js->clj
      ([x options]
       (let [{:keys [keywordize-keys]} options
             keyfn (if keywordize-keys keyword str)]
         (dissoc
           (into
             {}
             (for [k (js-keys x)]
               ;; ignore properties that are added with IEncodeClojure
               (if (#{"cljs$core$IEncodeClojure$"
                      "cljs$core$IEncodeClojure$_js__GT_clj$arity$2"}
                     k)
                 [nil nil]
                 (let [v (aget x k)]
                   ;; check if property is of List type and wasn't succesfully
                   ;; transformed to ClojureScript data structure
                   (when (and v
                              (not (string? v))
                              (not (boolean? v))
                              (not (number? v))
                              (not (coll? v))
                              (not (satisfies? IEncodeClojure v))
                              (str/includes? (type->str (type v)) "List"))
                     (add-js->clj-object (type v)))
                   [(keyfn k) (js->clj v :keywordize-keys keywordize-keys)]))))
           nil))))))

(defn check-collection
  "Checks if collection was succesfully transformed to ClojureScript,
   extends it with IEncodeClojure if necessary"
  [coll]
  (cond
    (not (coll? coll))
    (do (add-js->clj-array (type coll))
        (check-collection (js->clj coll :keywordize-keys true)))

    (let [f (first coll)]
      (and f (not (map? f))))
    (do (add-js->clj-object (type (first coll)))
        (js->clj coll :keywordize-keys true))

    :else coll))

(defn realm-collection->list [collection]
  (-> collection
      (.map (fn [object _ _] object))
      (js->clj :keywordize-keys true)
      check-collection))

(defn list->array [record list-field]
  (update-in record [list-field] (comp vec vals)))

(defn single [result]
  (-> (aget result 0)))

(defn single-cljs [result]
  (let [res (some-> (aget result 0)
                    (js->clj :keywordize-keys true))]
    (if (and res (not (map? res)))
      (do (add-js->clj-object (type res))
          (js->clj res :keywordize-keys true))
      res)))

(defn- get-schema-by-name [opts]
  (->> opts
       (mapv (fn [{:keys [name] :as schema}]
               [(keyword name) schema]))
       (into {})))

(defn- field-type [realm schema-name field]
  (let [schema-by-name (get-schema-by-name (js->clj (.-schema realm) :keywordize-keys true))
        field-def      (get-in schema-by-name [schema-name :properties field])]
    (if (map? field-def)
      (:type field-def)
      field-def)))

(defmulti to-query (fn [_ _ operator _ _] operator))

(defmethod to-query :eq [schema schema-name _ field value]
  (let [value         (to-string value)
        field-type    (field-type schema schema-name field)
        escaped-value (when value (gstr/escapeString (str value)))
        query         (str (name field) "=" (if (= "string" (name field-type))
                                              (str "\"" escaped-value "\"")
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
