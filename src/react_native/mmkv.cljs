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

(defn- clj->transit [o] 
  (transit/write writer o))

(defn- transit->clj [o]
  (try 
    (when o
      (transit/read reader o))
    (catch :default e
      (log/error "[mmkv] Error parsing transit data:" e)
      nil)))

;; Basic MMKV operations
(defn set-string
  "Store a string value in MMKV"
  [key value]
  (.set ^js storage key value))

(defn get-string
  "Get a string value from MMKV"
  ([key]
   (get-string key nil))
  ([key default-value]
   (let [value (.getString ^js storage key)]
     (if (nil? value)
       default-value
       value))))

(defn set-boolean
  "Store a boolean value in MMKV"
  [key value]
  (.set ^js storage key value))

(defn get-boolean
  "Get a boolean value from MMKV"
  ([key]
   (get-boolean key false))
  ([key default-value]
   (let [value (.getBoolean ^js storage key)]
     (if (nil? value)
       default-value
       value))))

(defn set-number
  "Store a number value in MMKV"
  [key value]
  (.set ^js storage key value))

(defn get-number
  "Get a number value from MMKV"
  ([key]
   (get-number key 0))
  ([key default-value]
   (let [value (.getNumber ^js storage key)]
     (if (nil? value)
       default-value
       value))))

(defn set-object
  "Store a ClojureScript data structure in MMKV using transit serialization"
  [key value]
  (let [transit-str (clj->transit value)]
    (.set ^js storage key transit-str)))

(defn get-object
  "Get a ClojureScript data structure from MMKV using transit deserialization"
  ([key]
   (get-object key nil))
  ([key default-value]
   (let [transit-str (.getString ^js storage key)]
     (if (nil? transit-str)
       default-value
       (or (transit->clj transit-str) default-value)))))

(defn contains-key?
  "Check if MMKV contains a key"
  [key]
  (.contains ^js storage key))

(defn delete-key
  "Delete a key from MMKV"
  [key]
  (.delete ^js storage key))

(defn clear-all
  "Clear all data from MMKV"
  []
  (.clearAll ^js storage))

(defn get-all-keys
  "Get all keys stored in MMKV"
  []
  (js->clj (.getAllKeys ^js storage)))
