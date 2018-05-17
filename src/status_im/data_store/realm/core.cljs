(ns status-im.data-store.realm.core
  (:require [goog.object :as object]
            [goog.string :as gstr]
            [clojure.string :as string]
            [status-im.data-store.realm.schemas.account.core :as account]
            [status-im.data-store.realm.schemas.base.core :as base]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [status-im.utils.async :as utils.async]
            [cognitect.transit :as transit]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.utils :as utils])
  (:refer-clojure :exclude [exists?]))

(defn to-buffer [key]
  (when key
    (let [length (.-length key)
          arr    (js/Uint8Array. length)]
      (dotimes [i length]
        (aset arr i (aget key i)))
      (.-buffer arr))))

(defn realm-version
  [file-name encryption-key]
  (if encryption-key
    (.schemaVersion rn-dependencies/realm file-name (to-buffer encryption-key))
    (.schemaVersion rn-dependencies/realm file-name)))

(defn open-realm
  [options file-name encryption-key]
  (log/debug "Opening realm at " file-name "...")
  (let [options-js (clj->js (assoc options :path file-name))]
    (when encryption-key
      (log/debug "Using encryption key...")
      (set! (.-encryptionKey options-js) (to-buffer encryption-key)))
    (when (cljs.core/exists? js/window)
      (rn-dependencies/realm. options-js))))

(defn- delete-realm
  [file-name]
  (.deleteFile rn-dependencies/realm (clj->js {:path file-name})))

(defn- close [realm]
  (when realm
    (.close realm)))

(defn migrate-realm [file-name schemas encryption-key]
  (let [current-version (realm-version file-name encryption-key)]
    (doseq [schema schemas
            :when (> (:schemaVersion schema) current-version)
            :let [migrated-realm (open-realm schema file-name encryption-key)]]
      (close migrated-realm)))
  (open-realm (last schemas) file-name encryption-key))

(defn reset-realm [file-name schemas encryption-key]
  (utils/show-popup "Important: Wallet Upgrade" "The Status Wallet will be upgraded in this release. The 12 mnemonic words will generate different addresses and whisper identities (public key). Given that we changed the algorithm used to generate keys and addresses, it will be impossible to re-import accounts created with the old algorithm in Status. Please create a new account.")
  (delete-realm file-name)
  (open-realm (last schemas) file-name encryption-key))

(defn- index-entity-schemas [all-schemas]
  (into {} (map (juxt :name identity)) (-> all-schemas last :schema)))

(def new-account-filename "new-account")

(def base-realm (atom nil))
(def account-realm (atom nil))

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
  (reset! base-realm (migrate-realm (.-defaultPath rn-dependencies/realm) base/schemas encryption-key))
  (log/debug "Created @base-realm"))

(defn reset-account-realm [encryption-key]
  (log/debug "Resetting account realm...")
  (when @account-realm
    (close @account-realm))
  (reset! account-realm (migrate-realm new-account-filename account/schemas encryption-key))
  (.write @account-realm #(.deleteAll @account-realm))
  (log/debug "Created @account-realm"))

(defn move-file-handler [address encryption-key err handler]
  (log/debug "Moved file with error: " err address)
  (if err
    (log/error "Error moving account realm: " (.-message err))
    (reset! account-realm (migrate-realm address account/schemas encryption-key)))
  (handler err))

(defn change-account [address new-account? encryption-key handler]
  (let [path (.-path @account-realm)]
    (log/debug "closing account realm: " path)
    (close-account-realm)
    (log/debug "is new account? " new-account?)
    (if new-account?
      (let [new-path (string/replace path new-account-filename address)]
        (log/debug "Moving file " path " to " new-path)
        (fs/move-file path new-path #(move-file-handler address encryption-key % handler)))
      (do
        (reset! account-realm (migrate-realm address account/schemas encryption-key))
        (handler nil)))))

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

(defn- list->clj [realm-list]
  (realm-list->clj-coll realm-list [] #(object/get realm-list %)))

(defn- object-list->clj [realm-object-list entity-name]
  (let [primary-key (-> entity->schemas (get entity-name) :primaryKey name)]
    (realm-list->clj-coll realm-object-list
                          {}
                          #(let [realm-obj (object/get realm-object-list %)]
                             [(object/get realm-obj primary-key) (realm-obj->clj realm-obj entity-name)]))))

(defn- realm-obj->clj [realm-obj entity-name]
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

(defn- field-type [realm schema-name field]
  (let [field-def (get-in entity->schemas [schema-name :properties field])]
    (or (:type field-def) field-def)))

(defmulti to-query (fn [_ _ operator _ _] operator))

(defmethod to-query :eq [schema schema-name _ field value]
  (let [field-type    (field-type schema schema-name field)
        escaped-value (when value (gstr/escapeString (str value)))
        query         (str (name field) "=" (if (= "string" (name field-type))
                                              (str "\"" escaped-value "\"")
                                              value))]
    query))

(defn get-by-field
  "Selects objects from realm identified by schema-name based on value of field"
  [realm schema-name field value]
  (let [q (to-query realm schema-name :eq field value)]
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
                       (to-query realm schema-name :eq k v))
                     fields)]
    (.filtered (.objects realm (name schema-name))
               (case op
                 :and (and-query queries)
                 :or (or-query queries)))))

(defn exists?
  "Returns true if object/s identified by schema-name and field and value
  exists in realm"
  [realm schema-name field value]
  (pos? (.-length (get-by-field realm schema-name field value))))
