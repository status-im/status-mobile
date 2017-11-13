(ns status-im.data-store.chats
  (:require [status-im.data-store.realm.chats :as data-store]
            [re-frame.core :refer [dispatch]])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-contacts
  [item]
  (update item :contacts vals))

(defn get-all
  []
  (map normalize-contacts (data-store/get-all-active)))

(defn get-by-id
  [id]
  (data-store/get-by-id id))

(defn exists?
  [chat-id]
  (data-store/exists? chat-id))

(defn save
  [{:keys [last-message-id chat-id] :as chat}]
  ;; TODO(janherich): remove `:last-message-id`, seems like it's not used anywhere anymore
  (let [chat (assoc chat :last-message-id (or last-message-id ""))]
    (data-store/save chat (data-store/exists? chat-id))))

(defn delete
  [chat-id]
  (data-store/delete chat-id))

(defn set-inactive
  [chat-id]
  (data-store/set-inactive chat-id))

(defn get-contacts
  [chat-id]
  (data-store/get-contacts chat-id))

(defn has-contact?
  [chat-id identity]
  (data-store/has-contact? chat-id identity))

(defn add-contacts
  [chat-id identities]
  (data-store/add-contacts chat-id identities)
  (dispatch [:reload-chats]))

(defn remove-contacts
  [chat-id identities]
  (data-store/remove-contacts chat-id identities))

(defn save-property
  [chat-id property-name value]
  (data-store/save-property chat-id property-name value))

(defn get-property
  [chat-id property-name]
  (data-store/get-property chat-id property-name))

(defn is-active?
  [chat-id]
  (get-property chat-id :is-active))

(defn removed-at
  [chat-id]
  (get-property chat-id :removed-at))

(defn get-message-overhead
  [chat-id]
  (get-property chat-id :message-overhead))

(defn get-active-group-chats
  []
  (data-store/get-active-group-chats))

(defn set-active
  [chat-id active?]
  (save-property chat-id :is-active active?))

(defn inc-message-overhead
  [chat-id]
  (save-property chat-id :message-overhead (inc (get-message-overhead chat-id))))

(defn reset-message-overhead
  [chat-id]
  (save-property chat-id :message-overhead 0))

(defn new-update?
  [timestamp chat-id]
  (let
    [{:keys [added-to-at removed-at removed-from-at added-at]}
     (get-by-id chat-id)]
    (and (> timestamp added-to-at)
         (> timestamp removed-at)
         (> timestamp removed-from-at)
         (> timestamp added-at))))
