(ns status-im.data-store.discover
  (:require [status-im.data-store.realm.discover :as data-store]))

(defn get-all
  [ordering]
  (->> (data-store/get-all-as-list ordering)
       (mapv #(update % :tags vals))))

(defn save
  [discover]
  (data-store/save discover))

(defn exists?
  [message-id]
  (data-store/exists? message-id))

(defn save-all
  [discoveries]
  (data-store/save-all discoveries))

(defn delete
  [by ordering critical-count to-delete-count]
  (data-store/delete by ordering critical-count to-delete-count))

(defn get-all-tags
  []
  (data-store/get-all-tags))