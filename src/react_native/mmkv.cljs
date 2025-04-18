(ns react-native.mmkv
  (:require
    ["react-native-mmkv" :refer [MMKV]]
    [cognitect.transit :as transit]
    [native-module.core :as native-module]
    [oops.core :as oops]
    [taoensso.timbre :as log])
  (:refer-clojure :exclude [set]))

;; Create a single MMKV instance to be used throughout the app with the path from native code
(defonce ^:private storage
  (let [mmkv-path (try
                    (native-module/get-mmkv-storage-path)
                    (catch :default _
                      ;; Fallback for tests
                      "/tmp/test-mmkv"))]
    (MMKV. #js {:id "mmkv.status" :path mmkv-path})))

;; Transit serialization/deserialization for ClojureScript data structures
(def ^:private reader (transit/reader :json))
(def ^:private writer (transit/writer :json))

(defn- clj->transit
  [o]
  (transit/write writer o))

(defn- transit->clj
  [o]
  (try
    (when o
      (transit/read reader o))
    (catch :default e
      (log/error "[mmkv] Error parsing transit data:" e)
      nil)))

;; Basic MMKV operations
(defn set
  "Store a value in MMKV."
  [k v]
  (oops/ocall storage "set" k v))

(defn get-string
  "Get a string value from MMKV"
  ([k]
   (get-string k nil))
  ([k default-value]
   (if-let [v (oops/ocall storage "getString" k)]
     v
     default-value)))

(defn get-boolean
  "Get a boolean value from MMKV"
  ([k]
   (get-boolean k false))
  ([k default-value]
   (let [v (oops/ocall storage "getBoolean" k)]
     (if (nil? v)
       default-value
       v))))

(defn get-number
  "Get a number value from MMKV"
  ([k]
   (get-number k 0))
  ([k default-value]
   (if-let [v (oops/ocall storage "getNumber" k)]
     v
     default-value)))

(defn set-object
  "Store a ClojureScript data structure in MMKV using transit serialization"
  [k v]
  (let [transit-str (clj->transit v)]
    (set k transit-str)))

(defn get-object
  "Get a ClojureScript data structure from MMKV using transit deserialization"
  ([k]
   (get-object k nil))
  ([k default-value]
   (if-let [transit-str (oops/ocall storage "getString" k)]
     (or (transit->clj transit-str) default-value)
     default-value)))

(defn contains-key?
  "Check if MMKV contains a key"
  [k]
  (oops/ocall storage "contains" k))

(defn delete-key
  "Delete a key from MMKV"
  [k]
  (oops/ocall storage "delete" k))

(defn clear-all
  "Clear all data from MMKV"
  []
  (oops/ocall storage "clearAll"))

(defn get-all-keys
  "Get all keys stored in MMKV"
  []
  (js->clj (oops/ocall storage "getAllKeys")))
