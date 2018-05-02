(ns status-im.data-store.realm.core
  (:require [status-im.utils.types :refer [to-string]]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [status-im.utils.async :as utils.async]
            [clojure.string :as str]
            [goog.string :as gstr]
            [cognitect.transit :as transit]
            [clojure.walk :as walk]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.utils :as utils])
  (:refer-clojure :exclude [exists?]))

(defn realm-version
  [file-name]
  (.schemaVersion rn-dependencies/realm file-name))

(defn open-realm
  [options file-name]
  (let [options (merge options {:path file-name})]
    (when (cljs.core/exists? js/window)
      (rn-dependencies/realm. (clj->js options)))))

(defn delete-realm
  [file-name]
  (.deleteFile rn-dependencies/realm (clj->js {:path file-name})))

(defn close [realm]
  (when realm
    (.close realm)))

(defn migrate-realm [file-name schemas]
  (let [current-version (realm-version file-name)]
    (doseq [schema schemas
            :when (> (:schemaVersion schema) current-version)
            :let [migrated-realm (open-realm schema file-name)]]
      (close migrated-realm)))
  (open-realm (last schemas) file-name))

(defn reset-realm [file-name schemas]
  (utils/show-popup "Please note" "You must recover or create a new account with this upgrade. Also chatting with accounts older then `0.9.17` is not possible")
  (delete-realm file-name)
  (open-realm (last schemas) file-name))

(defn open-migrated-realm
  [file-name schemas]
  ;; TODO: remove for release 0.9.18
  ;; delete the realm file if its schema version is lower
  ;; than existing schema version - dirty hotfix for `0.9.17` -> `0.9.18` upgrade
  (if (< (realm-version file-name)
         (apply max :schemaVersion base/schemas))
    (reset-realm file-name schemas)
    (migrate-realm file-name schemas)))

(def new-account-filename "new-account")

(def base-realm (open-migrated-realm (.-defaultPath rn-dependencies/realm) base/schemas))

(def account-realm (atom (open-migrated-realm new-account-filename account/schemas)))

(def realm-queue (utils.async/task-queue 2000))

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

;; realm functions

(defn and-query [queries]
  (str/join " and " queries))

(defn or-query [queries]
  (str/join " or " queries))

(defn write [realm f]
  (.write realm f))


(def transit-special-chars #{"~" "^" "`"})
(def transit-escape-char "~")

(defn to-be-escaped?
  "Check if element is a string that begins
   with a character recognized as special by Transit"
  [e]
  (and (string? e)
       (contains? transit-special-chars (first e))))

(defn prepare-for-transit
  "Following Transit documentation, escape leading special characters
  in strings by prepending a ~. This prepares for subsequent
  fetching from Realm where Transit is used for JSON parsing"
  [message]
  (let [walk-fn (fn [e]
                  (cond->> e
                    (to-be-escaped? e)
                    (str transit-escape-char)))]
    (walk/postwalk walk-fn message)))

(defn create
  ([realm schema-name obj]
   (create realm schema-name obj false))
  ([realm schema-name obj update?]
   (.create realm (to-string schema-name) (clj->js (prepare-for-transit obj)) update?)))

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

(def map->vec
  (comp vec vals))

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn- internal-convert [js-object]
  (->> js-object
       (.stringify js/JSON)
       deserialize
       walk/keywordize-keys))

(defn js-object->clj
  "Converts any js type/object into a map recursively
  Performs 5 times better than iterating over the object keys
  and that would require special care for collections"
  [js-object]
  (let [o (internal-convert js-object)]
    (if (map? o) (map->vec o) o)))

(defn fix-map->vec
  "Takes a map m and a keyword k
  Updates the value in k, a map representing a list, into a vector
  example: {:0 0 :1 1} -> [0 1]"
  [m k]
  (update m k map->vec))

(defn fix-map
  "Takes a map m, a keyword k and an id id
  Updates the value in k, a map representing a list, into a map using
  the id extracted from the value as a key
  example: {:0 {:id 1 :a 2} :1 {:id 2 :a 2}} -> {1 {:id 1 :a 2} 2 {:id 2 :a 2}}"
  [m k id]
  (update m k #(reduce (fn [acc [_ v]]
                         (assoc acc (get v id) v))
                       {}
                       %)))

(defn single [result]
  (aget result 0))

(defn single-clj [results]
  (some-> results single internal-convert))

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
  (single-clj (get-by-field realm schema-name field value)))

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
