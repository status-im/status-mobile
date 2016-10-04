(ns status-im.data-store.discovery
  (:require [status-im.data-store.realm.discovery :as data-store]))

(defn get-all
  []
  (->> (data-store/get-all-as-list)
       (mapv #(update % :tags vals))))

(defn save
  [discovery]
  (data-store/save discovery))

(defn save-all
  [discoveries]
  (data-store/save-all discoveries))

(defn delete
  [by ordering critical-count to-delete-count]
  (data-store/delete by ordering critical-count to-delete-count))

(defn get-all-tags
  []
  (data-store/get-all-tags))