(ns status-im.data-store.handler-data
  (:require [cljs.reader :as reader]
            [status-im.data-store.realm.handler-data :as data-store]
            [taoensso.timbre :as log]))

(defn get-all [] 
  (->> (data-store/get-all-as-list)
       (map (fn [{:keys [message-id data]}]
              [message-id (reader/read-string data)]))
       (into {})))

(defn get-data [message-id]
  (-> message-id data-store/get-by-message-id :data reader/read-string))

(defn save-data [handler-data]
  (data-store/save (update handler-data :data pr-str)))
