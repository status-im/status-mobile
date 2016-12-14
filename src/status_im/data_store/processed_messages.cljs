(ns status-im.data-store.processed-messages
  (:require [status-im.data-store.realm.processed-messages :as data-store]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn get-filtered
  [condition]
  (data-store/get-filtered-as-list condition))

(defn save
  [processed-message]
  (data-store/save processed-message))

(defn delete [condition]
  (data-store/delete condition))
