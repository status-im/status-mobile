(ns status-im.async-storage.core
  (:require ["@react-native-async-storage/async-storage" :default async-storage]
            [goog.functions :as f]
            [re-frame.core :as re-frame]
            [status-im.async-storage.transit :refer [clj->transit transit->clj]]
            [taoensso.timbre :as log]))

(def ^:private debounce-ms 250)

(def key->string str)

(defn set-item!
  [k value]
  (-> ^js async-storage
      (.setItem (key->string k)
                (clj->transit value))
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))

(defn- set-item-factory
  []
  (let [tmp-storage (atom {})
        debounced   (f/debounce (fn []
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
      (.multiGet (to-array (map key->string ks)))
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
      (.getItem (key->string k))
      (.then (fn [^js data]
               (-> data
                   js->clj
                   transit->clj
                   cb)))
      (.catch (fn [error]
                (cb nil)
                (log/error "[async-storage]" error)))))

(re-frame/reg-fx ::set! (set-item-factory))

(re-frame/reg-fx
 ::get
 (fn [{ks :keys cb :cb}]
   (get-items ks cb)))
