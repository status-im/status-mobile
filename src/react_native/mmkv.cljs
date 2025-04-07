(ns react-native.mmkv
  (:require
    ["react-native-mmkv" :refer [MMKV]]
    [cognitect.transit :as transit]
    [taoensso.timbre :as log]))

;; Create a single MMKV instance to be used throughout the app
(defonce ^:private storage (MMKV.))

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
(defn store
  "Store a value in MMKV."
  [k v]
  (.set ^js storage k v))

(defn get-string
  "Get a string value from MMKV"
  ([k]
   (get-string k nil))
  ([k default-value]
   (if-let [v (.getString ^js storage k)]
     v
     default-value)))

(defn get-boolean
  "Get a boolean value from MMKV"
  ([k]
   (get-boolean k false))
  ([k default-value]
   (let [v (.getBoolean ^js storage k)]
     (if (nil? v)
       default-value
       v))))

(defn get-number
  "Get a number value from MMKV"
  ([k]
   (get-number k 0))
  ([k default-value]
   (if-let [v (.getNumber ^js storage k)]
     v
     default-value)))

(defn set-object
  "Store a ClojureScript data structure in MMKV using transit serialization"
  [k v]
  (let [transit-str (clj->transit v)]
    (.set ^js storage k transit-str)))

(defn get-object
  "Get a ClojureScript data structure from MMKV using transit deserialization"
  ([k]
   (get-object k nil))
  ([k default-value]
   (if-let [transit-str (.getString ^js storage k)]
     (or (transit->clj transit-str) default-value)
     default-value)))

(defn contains-key?
  "Check if MMKV contains a key"
  [k]
  (.contains ^js storage k))

(defn delete-key
  "Delete a key from MMKV"
  [k]
  (.delete ^js storage k))

(defn clear-all
  "Clear all data from MMKV"
  []
  (.clearAll ^js storage))

(defn get-all-keys
  "Get all keys stored in MMKV"
  []
  (js->clj (.getAllKeys ^js storage)))
