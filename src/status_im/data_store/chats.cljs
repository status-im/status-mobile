(ns status-im.data-store.chats
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.chats :as data-store])
  (:refer-clojure :exclude [exists?]))

(re-frame/reg-cofx
  :data-store/all-chats
  (fn [cofx _]
    (assoc cofx :all-stored-chats (data-store/get-all-active))))

(re-frame/reg-cofx
  :data-store/inactive-chat-ids
  (fn [cofx _]
    (assoc cofx :inactive-chat-ids (data-store/get-inactive-ids))))

(re-frame/reg-cofx
  :data-store/get-chat
  (fn [cofx _]
    (assoc cofx :get-stored-chat data-store/get-by-id)))

(re-frame/reg-fx
  :data-store/save-chat
  (fn [{:keys [chat-id] :as chat}]
    (async/go (async/>! core/realm-queue #(data-store/save chat (data-store/exists? chat-id))))))

(re-frame/reg-fx
  :data-store/delete-chat
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/delete chat-id)))))

(re-frame/reg-fx
  :data-store/deactivate-chat
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/set-inactive chat-id)))))

(re-frame/reg-fx
  :data-store/add-chat-contacts
  (fn [[chat-id contacts]]
    (async/go (async/>! core/realm-queue #(data-store/add-contacts chat-id contacts)))))

(re-frame/reg-fx
  :data-store/remove-chat-contacts
  (fn [[chat-id contacts]]
    (async/go (async/>! core/realm-queue #(data-store/remove-contacts chat-id contacts)))))

(re-frame/reg-fx
  :data-store/save-chat-property
  (fn [[chat-id prop value]]
    (async/go (async/>! core/realm-queue #(data-store/save-property chat-id prop value)))))
