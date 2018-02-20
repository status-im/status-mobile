(ns status-im.data-store.browser
  (:require [status-im.data-store.realm.browser :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  []
  (data-store/get-all))

(defn get-by-id
  [id]
  (data-store/get-by-id id))

(defn exists?
  [browser-id]
  (data-store/exists? browser-id))

(defn save
  [{:keys [browser-id] :as browser}]
  (data-store/save browser (exists? browser-id)))

(defn delete
  [browser-id]
  (data-store/delete (exists? browser-id)))