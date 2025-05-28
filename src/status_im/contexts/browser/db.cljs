(ns status-im.contexts.browser.db
  (:require [status-im.contexts.browser.constants :as browser.constants]))

(defn get-tab-ids
  [db]
  (get db :browser/tab-ids))

(defn get-tabs-by-id
  [db]
  (get db :browser/tabs-by-id))

(defn get-dapps
  [db]
  (get db :browser/dapps))

(defn get-dapp-by-id
  [db dapp-id]
  (-> db get-dapps (get dapp-id)))

(defn get-tab-id-by-index
  [db idx]
  (-> db get-tab-ids (nth idx 0)))

(defn get-tab
  [db tab-id]
  (-> db get-tabs-by-id (get tab-id)))

(defn get-tab-url
  [db tab-id]
  (-> db (get-tab tab-id) :url))

(defn get-dapp-id
  [db tab-id]
  (-> db (get-tab tab-id) :dapp-id))

(defn get-tab-dapp
  [db tab-id]
  (->> (get-dapp-id db tab-id)
       (get-dapp-by-id db)))

(defn get-dapp-permissions
  [db]
  (get db :browser/permissions))

(defn get-dapp-chain-id
  [db dapp-url]
  (-> db
      get-dapp-permissions
      (get dapp-url)
      (get :chain-id browser.constants/default-chain-id)))
