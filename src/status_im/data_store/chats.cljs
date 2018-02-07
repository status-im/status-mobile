(ns status-im.data-store.chats
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.chats :as data-store])
  (:refer-clojure :exclude [exists?]))

(re-frame/reg-cofx
  :all-stored-chats
  (fn [cofx _]
    (assoc cofx :all-stored-chats (data-store/get-all-active))))

(re-frame/reg-cofx
  :inactive-chat-ids
  (fn [cofx _]
    (assoc cofx :inactive-chat-ids (data-store/get-inactive-ids))))

(re-frame/reg-cofx
  :get-stored-chat
  (fn [cofx _]
    (assoc cofx :get-stored-chat data-store/get-by-id)))

(re-frame/reg-fx
  :save-chat
  (fn [{:keys [chat-id] :as chat}]
    (async/go (async/>! core/realm-queue #(data-store/save chat (data-store/exists? chat-id))))))

(re-frame/reg-fx
  :delete-chat
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/delete chat-id)))))

(re-frame/reg-fx
  :deactivate-chat
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/set-inactive chat-id)))))

(defn get-contacts
  [chat-id]
  (data-store/get-contacts chat-id))

(defn add-contacts
  [chat-id identities]
  (data-store/add-contacts chat-id identities))

(defn remove-contacts
  [chat-id identities]
  (data-store/remove-contacts chat-id identities))

(defn save-property
  [chat-id property-name value]
  (data-store/save-property chat-id property-name value))

(defn get-property
  [chat-id property-name]
  (data-store/get-property chat-id property-name))

(defn removed-at
  [chat-id]
  (get-property chat-id :removed-at))

(defn get-active-group-chats
  []
  (data-store/get-active-group-chats))

(defn set-active
  [chat-id active?]
  (save-property chat-id :is-active active?))
