(ns status-im.data-store.realm.core
  (:require [clojure.string :as string]
            [cognitect.transit :as transit]
            [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [status-im.ethereum.core :as ethereum]
            [status-im.js-dependencies :as js-dependencies]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.async :as utils.async]
            [status-im.utils.fs :as fs]
            [status-im.utils.platform :as utils.platform]
            [taoensso.timbre :as log]))

(defn to-buffer [key]
  (when-not (nil? key)
    (when key
      (let [length (.-length ^js key)
            arr    (js/Int8Array. length)]
        (dotimes [i length]
          (aset arr i (aget key i)))
        arr))))

(defn encrypted-realm-version
  "Returns -1 if the file does not exists, the schema version if it successfully
  decrypts it, throws error otherwise."
  [file-name encryption-key]
  (if encryption-key
    (.schemaVersion ^js rn-dependencies/realm file-name (to-buffer encryption-key))
    (.schemaVersion ^js rn-dependencies/realm file-name)))

(defn encrypted-realm-version-promise
  [file-name encryption-key]
  (js/Promise.
   (fn [on-success on-error]
     (try
       (encrypted-realm-version file-name encryption-key)
       (on-success)
       (catch :default e
         (on-error {:message (str e)
                    :error   :decryption-failed}))))))

(defn open-realm
  [options file-name encryption-key]
  (log/debug "Opening realm at " file-name "...")
  (let [options-js (clj->js (assoc options :path file-name))]
    (log/debug "Using encryption key...")
    (when encryption-key
      (set! (.-encryptionKey ^js options-js) (to-buffer encryption-key)))
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
  (.-defaultPath ^js rn-dependencies/realm))

(defn realm-dir []
  "This has to be a fn because otherwise re-frame app-db is not
  initialized yet"
  (if-let [path (utils.platform/no-backup-directory)]
    (str path "/realm/")
    (let [initial-props @(re-frame/subscribe [:initial-props])
          status-data-dir (get initial-props :STATUS_DATA_DIR)]
      (cond-> (if status-data-dir
                (str status-data-dir "/default.realm")
                (.-defaultPath ^js rn-dependencies/realm))
        utils.platform/desktop?
        (str "/")))))

(def old-realm-dir
  (string/replace old-base-realm-path #"default\.realm$" ""))

(defn accounts-realm-dir []
  (str (realm-dir) "accounts/"))

(defn base-realm-path []
  (str (realm-dir) "default.realm"))

(defn get-account-db-path
  [address]
  (str (accounts-realm-dir) (ethereum/sha3 address)))

(defn delete-realms []
  (log/warn "realm: deleting all realms")
  (fs/unlink (realm-dir)))

(defn delete-account-realm
  [address]
  (log/warn "realm: deleting account db " (ethereum/sha3 address))
  (let [file (get-account-db-path address)]
    (.. ^js (fs/unlink file)
        (then #(fs/unlink (str file ".lock")))
        (then #(fs/unlink (str file ".management")))
        (then #(fs/unlink (str file ".note"))))))

(defn ensure-directories []
  (.. ^js (fs/mkdir (realm-dir))
      (then #(fs/mkdir (accounts-realm-dir)))))

(defn- move-realm-to-library [path]
  (let [filename (last (string/split path "/"))
        new-path (if (is-account-file? path)
                   (get-account-db-path filename)
                   (str (realm-dir) filename))]
    (log/debug "realm: moving " path " to " new-path)
    (if (realm-management-file? path)
      (fs/unlink path)
      (fs/move-file path new-path))))

(defn move-realms []
  (log/info "realm: moving all realms")
  (..
   ^js (fs/read-dir old-realm-dir)
   (then #(->> (js->clj % :keywordize-keys true)
               (map :path)
               (filter is-realm-file?)))
   (then #(js/Promise.all (clj->js (map move-realm-to-library %))))))

(defn- close [realm]
  (when realm
    (.close realm)))

(defonce schema-migration-log (atom {}))

(defn migration-log [k v]
  (swap! schema-migration-log assoc k v))

(defn- migrate-schemas
  "Apply migrations in sequence and open database with the last schema"
  [file-name schemas encryption-key current-version]
  (reset! schema-migration-log {})
  (migration-log :initial-version current-version)
  (migration-log :current-version current-version)
  (migration-log :last-version (:schemaVersion (last schemas)))
  (log/info "migrate schemas" current-version)
  (when (pos? current-version)
    (doseq [schema schemas
            :when (> (:schemaVersion schema) current-version)]
      (migration-log :current-version (:schemaVersion schema))
      (let [migrated-realm (open-realm schema file-name encryption-key)]
        (close migrated-realm))))
  (open-realm (last schemas) file-name encryption-key))

(defn keccak512-array [key]
  (.array (.-keccak512 ^js (js-dependencies/js-sha3)) key))

(defn merge-Uint8Arrays [^js arr1 ^js arr2]
  (let [arr1-length (.-length arr1)
        arr2-length (.-length arr2)
        arr         (js/Uint8Array. (+ arr1-length arr2-length))]
    (.set arr arr1)
    (.set arr arr2 arr1-length)
    arr))

(defn db-encryption-key [password encryption-key]
  (let [TextEncoder (.-TextEncoder ^js (js-dependencies/text-encoding))
        password-array (.encode
                        ^js (new TextEncoder)
                        password)]
    (keccak512-array (merge-Uint8Arrays encryption-key password-array))))

(defn migrate-realm
  "Migrate realm if is a compatible version or reset the database"
  [file-name schemas encryption-key]
  (log/info "migrate-realm")
  (migrate-schemas file-name schemas encryption-key (encrypted-realm-version
                                                     file-name
                                                     encryption-key)))

(defn- index-entity-schemas [all-schemas]
  (into {} (map (juxt :name identity)) (-> all-schemas last :schema)))

(defonce base-realm (atom nil))
(defonce account-realm (atom nil))

(def entity->schemas (merge (index-entity-schemas base/schemas)
                            (index-entity-schemas account/schemas)))

(def realm-queue (utils.async/task-queue 2000))

(defn close-account-realm []
  (log/debug "closing account realm")
  (close @account-realm)
  (reset! account-realm nil))

(defn open-base-realm [encryption-key]
  (log/debug "Opening base realm... (first run)")
  (when @base-realm
    (close @base-realm))
  (reset! base-realm (migrate-realm (base-realm-path) base/schemas encryption-key))
  (log/debug "Created @base-realm"))

(defn re-encrypt-realm
  [file-name old-key new-key on-success on-error]
  (let [old-file-name (str file-name "old")]
    (.. ^js (fs/move-file file-name old-file-name)
        (then #(fs/unlink (str file-name ".lock")))
        (then #(fs/unlink (str file-name ".management")))
        (then #(fs/unlink (str file-name ".note")))
        (catch (fn [e]
                 (let [message (str "can't move old database " (str e) " " file-name)]
                   (log/debug message)
                   (on-error {:message message
                              :error   :removing-old-db-failed}))))
        (then (fn []
                (let [old-account-db (migrate-realm old-file-name
                                                    account/schemas
                                                    old-key)]
                  (log/info "copy old database")
                  (.writeCopyTo ^js old-account-db file-name (to-buffer new-key))
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
                   (on-error {:error   :write-copy-to-failed
                              :message message})))))))

(defn check-db-encryption
  [address password old-key]
  (let [file-name (get-account-db-path address)
        new-key   (db-encryption-key password old-key)]
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
             (.. ^js (encrypted-realm-version-promise file-name old-key)
                 (then
                  #(re-encrypt-realm file-name old-key new-key on-success on-error))
                 (catch on-error)))))))))

(defn db-exists? [address]
  (js/Promise.
   (fn [on-success on-error]
     (.. ^js (fs/file-exists? (get-account-db-path address))
         (then (fn [db-exists?]
                 (if db-exists?
                   (on-success)
                   (on-error {:message "Account's database doesn't exist."
                              :error   :database-does-not-exist}))))))))

(defn open-account [address password encryption-key]
  (let [path (get-account-db-path address)
        account-db-key (db-encryption-key password encryption-key)]
    (js/Promise.
     (fn [on-success on-error]
       (try
         (log/info "open-account")
         (reset! account-realm
                 (migrate-realm path account/schemas account-db-key))
         (log/info "account-realm " (nil? @account-realm))
         (on-success)
         (catch :default e
           (on-error {:message (str e)
                      :error   :migrations-failed
                      :details @schema-migration-log})))))))

(declare realm-obj->clj)

;; realm functions

(defn write [realm f]
  (.write ^js realm f))

(defn create
  ([realm schema-name obj]
   (create realm schema-name obj false))
  ([realm schema-name obj update?]
   (let [obj-to-save (select-keys obj (keys (get-in entity->schemas
                                                    [schema-name :properties])))]
     (.create ^js realm (name schema-name) (clj->js obj-to-save) update?))))

(defn delete [realm obj]
  (.delete ^js realm obj))

(defn get-all [realm schema-name]
  (.objects ^js realm (name schema-name)))

(defn sorted [results field-name order]
  (.sorted ^js results (name field-name) (if (= order :asc)
                                           false
                                           true)))

(defn multi-field-sorted [results fields]
  (.sorted ^js results (clj->js fields)))

(defn page [results from to]
  (js/Array.prototype.slice.call results from (or to -1)))

(defn filtered [results filter-query]
  (.filtered ^js results filter-query))

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
    (.filtered (.objects ^js realm (name schema-name)) q)))

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
    (.filtered (.objects ^js realm (name schema-name))
               (case op
                 :and (and-query queries)
                 :or (or-query queries)))))

(defn in-query
  "Constructs IN query"
  [field-name ids]
  (string/join " or " (map #(str field-name "=\"" % "\"") ids)))
