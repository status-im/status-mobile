(ns status-im.async-storage.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [goog.functions :as f]
            [status-im.async-storage.transit :refer [clj->transit transit->clj]]
            ["@react-native-community/async-storage" :default async-storage]))

(def ^:private debounce-ms 250)

(def key->string str)

(defn- set-item! [key value]
  (-> ^js async-storage
      (.setItem (key->string key)
                (clj->transit value))
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))

(defn- set-item-factory
  []
  (let [tmp-storage (atom {})
        debounced   (f/debounce (fn []
                                  (doseq [[k v] @tmp-storage]
                                    (swap! tmp-storage dissoc k)
                                    (set-item! k v))) debounce-ms)]
    (fn [items]
      (swap! tmp-storage merge items)
      (debounced))))

(defn get-items [keys cb]
  (-> ^js async-storage
      (.multiGet (to-array (map key->string keys)))
      (.then (fn [^js data]
               (cb (->> (js->clj data)
                        (map (comp transit->clj second))
                        (zipmap keys)))))
      (.catch (fn [error]
                (cb nil)
                (log/error "[async-storage]" error)))))

(defn get-item [k cb]
  (-> ^js async-storage
      (.getItem (key->string k))
      (.then cb)
      (.catch (fn [error]
                (cb nil)
                (log/error "[async-storage]" error)))))

(re-frame/reg-fx ::set! (set-item-factory))

(re-frame/reg-fx
 ::get
 (fn [{:keys [keys cb]}]
   (get-items keys cb)))
