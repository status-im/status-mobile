(ns status-im.data-store.chats
  (:require [goog.object :as object]
            [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.realm.core :as core]))

(defn- normalize-chat [{:keys [chat-id] :as chat}]
  (let [last-clock-value (-> (core/get-by-field @core/account-realm :message :chat-id chat-id)
                             (core/sorted :clock-value :desc)
                             (core/single-clj :message)
                             :clock-value)]
    (assoc chat :last-clock-value  (or last-clock-value 0))))

(re-frame/reg-cofx
  :data-store/all-chats
  (fn [cofx _]
    (assoc cofx :all-stored-chats (map normalize-chat
                                       (-> @core/account-realm
                                           (core/get-all :chat)
                                           (core/sorted :timestamp :desc)
                                           (core/all-clj :chat))))))


(defn save-chat-tx
  "Returns tx function for saving chat"
  [{:keys [chat-id] :as chat}]
  (fn [realm]
    (core/create realm :chat chat (core/exists? realm :chat :chat-id chat-id))))

;; Only used in debug mode
(defn delete-chat-tx
  "Returns tx function for hard deleting the chat"
  [chat-id]
  (fn [realm]
    (core/delete realm (core/get-by-field realm :chat :chat chat-id))))

(defn- get-chat-by-id [chat-id realm]
  (core/single (core/get-by-field realm :chat :chat-id chat-id)))

(defn deactivate-chat-tx
  "Returns tx function for deactivating chat"
  [chat-id now]
  (fn [realm]
    (let [chat (get-chat-by-id chat-id realm)]
      (doto chat
        (aset "is-active" false)
        (aset "removed-at" now)))))

(defn add-chat-contacts-tx
  "Returns tx function for adding chat contacts"
  [chat-id contacts]
  (fn [realm]
    (let [chat              (get-chat-by-id chat-id realm)
          existing-contacts (object/get chat "contacts")]
      (aset chat "contacts" (clj->js (into #{} (concat contacts
                                                       (core/list->clj existing-contacts))))))))

(defn remove-chat-contacts-tx
  "Returns tx function for removing chat contacts"
  [chat-id contacts]
  (fn [realm]
    (let [chat              (get-chat-by-id chat-id realm)
          existing-contacts (object/get chat "contacts")]
      (aset chat "contacts" (clj->js (remove (into #{} contacts)
                                             (core/list->clj existing-contacts)))))))
