(ns status-im.data-store.realm.core
  (:require [goog.object :as object]
            [goog.string :as gstr]
            [clojure.string :as string]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [status-im.utils.async :as utils.async]
            [status-im.utils.platform :as utils.platform]
            [status-im.utils.ethereum.core :as utils.ethereum]
            [cognitect.transit :as transit]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.js-dependencies :as js-dependencies]))

(defn to-buffer [key]
  (when-not (nil? key)
    (when key
      (let [length (.-length key)
            arr    (js/Int8Array. length)]
        (dotimes [i length]
          (aset arr i (aget key i)))
        arr))))

(defn encrypted-realm-version
  "Returns -1 if the file does not exists, the schema version if it successfully
  decrypts it, throws error otherwise."
  [file-name encryption-key]
  (if encryption-key
    (.schemaVersion rn-dependencies/realm file-name (to-buffer encryption-key))
    (.schemaVersion rn-dependencies/realm file-name)))

(defn open-realm
  [options file-name encryption-key]
  (log/debug "Opening realm at " file-name "...")
  (let [options-js (clj->js (assoc options :path file-name))]
    (log/debug "Using encryption key...")
    (when encryption-key
      (set! (.-encryptionKey options-js) (to-buffer encryption-key)))
    (when (exists? js/window)
      (rn-dependencies/realm. options-js))))

(defn- is-account-file? [n]
  (re-matches #".*/[0-9a-f]{40}$" n))

(defn- is-realm-file? [n]
  (or (re-matches #".*/default\.realm(\.management|\.lock|\.note)?$" n)
      (re-matches #".*/new-account(\.management|\.lock|\.note)?$" n)
      (re-matches #".*/[0-9a-f]{40}(\.management|\.lock|\.note)?$" n)))

(defn- realm-management-file? [n]
  (re-matches #".*(\.management|\.lock|\.note)$" n))

(def old-base-realm-path
  (.-defaultPath rn-dependencies/realm))

(def realm-dir
  (if-let [path (utils.platform/no-backup-directory)]
    (str path "/realm/")
    (.-defaultPath rn-dependencies/realm)))

(def old-realm-dir
  (string/replace old-base-realm-path #"default\.realm$" ""))

(def accounts-realm-dir
  (str realm-dir "accounts/"))

(def base-realm-path
  (str realm-dir
       "default.realm"))

(defn delete-realms []
  (log/warn "realm: deleting all realms")
  (fs/unlink realm-dir))

(defn ensure-directories []
  (..
   (fs/mkdir realm-dir)
   (then #(fs/mkdir accounts-realm-dir))))

(defn- move-realm-to-library [path]
  (let [filename (last (string/split path "/"))
        new-path (if (is-account-file? path)
                   (str accounts-realm-dir (utils.ethereum/sha3 filename))
                   (str realm-dir filename))]
    (log/debug "realm: moving " path " to " new-path)
    (if (realm-management-file? path)
      (fs/unlink path)
      (fs/move-file path new-path))))

(defn move-realms []
  (log/info "realm: moving all realms")
  (..
   (fs/read-dir old-realm-dir)
   (then #(->> (js->clj % :keywordize-keys true)
               (map :path)
               (filter is-realm-file?)))
   (then #(js/Promise.all (clj->js (map move-realm-to-library %))))))

(defn- close [realm]
  (when realm
    (.close realm)))

(defn- migrate-schemas
  "Apply migrations in sequence and open database with the last schema"
  [file-name schemas encryption-key current-version]
  (log/info "migrate schemas")
  (doseq [schema schemas
          :when (> (:schemaVersion schema) current-version)
          :let [migrated-realm (open-realm schema file-name encryption-key)]]
    (close migrated-realm))
  (open-realm (last schemas) file-name encryption-key))

(defn keccak512-array [key]
  (.array (.-keccak512 js-dependencies/js-sha3) key))

(defn merge-Uint8Arrays [arr1 arr2]
  (let [arr1-length (.-length arr1)
        arr2-length (.-length arr2)
        arr         (js/Uint8Array. (+ arr1-length arr2-length))]
    (.set arr arr1)
    (.set arr arr2 arr1-length)
    arr))

(defn db-encryption-key [password encryption-key]
  (let [password-array (.encode
                        (new (.-TextEncoder js-dependencies/text-encoding))
                        password)]
    (keccak512-array (merge-Uint8Arrays encryption-key password-array))))

(defn migrate-realm
  "Migrate realm if is a compatible version or reset the database"
  [file-name schemas encryption-key]
  (log/info "migrate-realm")
  (migrate-schemas file-name schemas encryption-key (encrypted-realm-version
                                                     file-name
                                                     encryption-key)))

(defn open-migrated-realm
  [file-name schemas encryption-key]
  (migrate-realm file-name schemas encryption-key))

(defn- index-entity-schemas [all-schemas]
  (into {} (map (juxt :name identity)) (-> all-schemas last :schema)))

(defonce base-realm (atom nil))
(defonce account-realm (atom nil))

(def entity->schemas (merge (index-entity-schemas base/schemas)
                            (index-entity-schemas account/schemas)))

(def realm-queue (utils.async/task-queue 2000))

(defn close-account-realm []
  (close @account-realm)
  (reset! account-realm nil))

(defn open-base-realm [encryption-key]
  (log/debug "Opening base realm... (first run)")
  (when @base-realm
    (close @base-realm))
  (reset! base-realm (open-migrated-realm base-realm-path base/schemas encryption-key))
  (log/debug "Created @base-realm"))

(defn re-encrypt-realm
  [file-name old-key new-key on-success on-error]
  (let [old-file-name (str file-name "old")]
    (.. (fs/move-file file-name old-file-name)
        (then #(fs/unlink (str file-name ".lock")))
        (then #(fs/unlink (str file-name ".management")))
        (then #(fs/unlink (str file-name ".note")))
        (catch (fn [e]
                 (let [message (str "can't move old database " (str e) " " file-name)]
                   (log/debug message)
                   (on-error {:error message}))))
        (then (fn []
                (let [old-account-db (open-migrated-realm old-file-name
                                                          account/schemas
                                                          old-key)]
                  (log/info "copy old database")
                  (.writeCopyTo old-account-db file-name (to-buffer new-key))
                  (log/info "old database copied")
                  (close old-account-db)
                  (log/info "old database closed")
                  (on-success)
                  (fs/unlink old-file-name)
                  (log/info "old database removed"))))
        (catch (fn [e]
                 (try (fs/move-file old-file-name file-name)
                      (catch :default _))
                 (let [message (str "something went wrong " (str e) " " file-name)]
                   (log/info message)
                   (on-error {:error message})))))))

(defn check-db-encryption
  [file-name old-key new-key]
  (js/Promise.
   (fn [on-success on-error]
     (try
       (do
         (log/info "try to encrypt with password")
         (encrypted-realm-version file-name new-key)
         (log/info "try to encrypt with password success")
         (on-success))
       (catch :default e
         (do
           (log/warn "failed checking db encryption with" e)
           (log/info "try to encrypt with old key")
           (encrypted-realm-version file-name old-key)
           (log/info "try to encrypt with old key success")
           (re-encrypt-realm file-name old-key new-key on-success on-error)))))))

(defn change-account [address password encryption-key]
  (let [path           (str accounts-realm-dir (utils.ethereum/sha3 address))
        account-db-key (db-encryption-key password encryption-key)]
    (close-account-realm)
    (..
     (check-db-encryption path encryption-key account-db-key)
     (then
      (fn []
        (log/info "change-account done" (nil? @account-realm))
        (reset! account-realm
                (open-migrated-realm path account/schemas account-db-key))
        (log/info "account-realm " (nil? @account-realm)))))))

(declare realm-obj->clj)

;; realm functions

(defn write [realm f]
  (.write realm f))

(defn create
  ([realm schema-name obj]
   (create realm schema-name obj false))
  ([realm schema-name obj update?]
   (let [obj-to-save (select-keys obj (keys (get-in entity->schemas
                                                    [schema-name :properties])))]
     (.create realm (name schema-name) (clj->js obj-to-save) update?))))

(defn delete [realm obj]
  (.delete realm obj))

(defn get-all [realm schema-name]
  (.objects realm (name schema-name)))

(defn sorted [results field-name order]
  (.sorted results (name field-name) (if (= order :asc)
                                       false
                                       true)))

(defn page [results from to]
  (js/Array.prototype.slice.call results from to))

(defn filtered [results filter-query]
  (.filtered results filter-query))

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn- realm-list->clj-coll [realm-list coll map-fn]
  (when realm-list
    (into coll (map map-fn) (range 0 (.-length realm-list)))))

(defn list->clj [realm-list]
  (realm-list->clj-coll realm-list [] #(object/get realm-list %)))

(defn- object-list->clj [realm-object-list entity-name]
  (let [primary-key (-> entity->schemas (get entity-name) :primaryKey name)]
    (realm-list->clj-coll realm-object-list
                          {}
                          #(let [realm-obj (object/get realm-object-list %)]
                             [(object/get realm-obj primary-key) (realm-obj->clj realm-obj entity-name)]))))

(defn realm-obj->clj [realm-obj entity-name]
  (when realm-obj
    (let [{:keys [primaryKey properties]} (get entity->schemas entity-name)]
      (into {}
            (map (fn [[prop-name {:keys [type objectType]}]]
                   (let [prop-value (object/get realm-obj (name prop-name))]
                     [prop-name (case type
                                  "string[]" (list->clj prop-value)
                                  :list (object-list->clj prop-value objectType)
                                  prop-value)])))
            properties))))

(defn single
  "Takes realm results, returns the first one"
  [result]
  (object/get result 0))

(defn single-clj
  "Takes realm results and schema name, returns the first result converted to cljs datastructure"
  [results schema-name]
  (-> results single (realm-obj->clj schema-name)))

(defn all-clj
  "Takes realm results and schema name, returns results as converted cljs datastructures in vector"
  [results schema-name]
  (realm-list->clj-coll results [] #(realm-obj->clj (object/get results %) schema-name)))

(defn- field-type [schema-name field]
  (let [field-def (get-in entity->schemas [schema-name :properties field])]
    (or (:type field-def) field-def)))

(defmulti to-query (fn [_ operator _ _] operator))

(defmethod to-query :eq [schema-name _ field value]
  (let [field-type (field-type schema-name field)
        query      (str (name field) "=" (if (= "string" (name field-type))
                                           (str "\"" value "\"")
                                           value))]
    query))

(defn get-by-field
  "Selects objects from realm identified by schema-name based on value of field"
  [realm schema-name field value]
  (let [q (to-query schema-name :eq field value)]
    (.filtered (.objects realm (name schema-name)) q)))

(defn- and-query [queries]
  (string/join " and " queries))

(defn- or-query [queries]
  (string/join " or " queries))

(defn get-by-fields
  "Selects objects from realm identified by schema name based on field values
  combined by `:and`/`:or` operator"
  [realm schema-name op fields]
  (let [queries (map (fn [[k v]]
                       (to-query schema-name :eq k v))
                     fields)]
    (.filtered (.objects realm (name schema-name))
               (case op
                 :and (and-query queries)
                 :or (or-query queries)))))

(defn in-query
  "Constructs IN query"
  [field-name ids]
  (string/join " or " (map #(str field-name "=\"" % "\"") ids)))
