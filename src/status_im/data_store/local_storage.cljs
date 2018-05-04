(ns status-im.data-store.local-storage
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.local-storage :as data-store]))

(re-frame/reg-cofx
 :data-store/get-local-storage-data
 (fn [cofx _]
   (assoc cofx :get-local-storage-data (comp :data data-store/get-by-chat-id))))

(re-frame/reg-fx
  :data-store/set-local-storage-data
  (fn [data]
    (async/go (async/>! core/realm-queue #(data-store/save data)))))
