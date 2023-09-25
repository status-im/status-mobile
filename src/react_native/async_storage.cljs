(ns react-native.async-storage
  (:require ["@react-native-async-storage/async-storage" :default async-storage]
            [cognitect.transit :as transit]
            [taoensso.timbre :as log]
            goog.functions))

(def ^:private debounce-ms 250)

(def ^:private reader (transit/reader :json))
(def ^:private writer (transit/writer :json))

(defn clj->transit [o] (transit/write writer o))
(defn transit->clj
  [o]
  (try (transit/read reader o)
       (catch :default e
         (log/error e))))

(defn set-item!
  [k value]
  (-> ^js async-storage
      (.setItem (str k)
                (clj->transit value))
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))

(defn set-item-factory
  []
  (let [tmp-storage (atom {})
        debounced   (goog.functions/debounce (fn []
                                               (doseq [[k v] @tmp-storage]
                                                 (swap! tmp-storage dissoc k)
                                                 (set-item! k v)))
                                             debounce-ms)]
    (fn [items]
      (swap! tmp-storage merge items)
      (debounced))))

(defn get-items
  [ks cb]
  (-> ^js async-storage
      (.multiGet (to-array (map str ks)))
      (.then (fn [^js data]
               (cb (->> (js->clj data)
                        (map (comp transit->clj second))
                        (zipmap ks)))))
      (.catch (fn [error]
                (cb nil)
                (log/error "[async-storage]" error)))))

(defn get-item
  [k cb]
  (-> ^js async-storage
      (.getItem (str k))
      (.then (fn [^js data]
               (-> data
                   js->clj
                   transit->clj
                   cb)))
      (.catch (fn [error]
                (cb nil)
                (log/error "[async-storage]" error)))))
