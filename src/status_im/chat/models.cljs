(ns status-im.chat.models
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.gfycat.core :as gfycat]))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn toggle-chat-ui-prop
  "Toggles chat ui prop in active chat"
  [{:keys [current-chat-id] :as db} ui-element]
  (update-in db [:chat-ui-props current-chat-id ui-element] not))

(defn- create-new-chat
  [{:keys [db now] :as cofx} chat-id chat-props]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    (merge {:chat-id    chat-id
            :name       (or name (gfycat/generate-gfy chat-id))
            :color      styles/default-chat-color
            :group-chat false
            :is-active  true
            :timestamp  now
            :contacts   [{:identity chat-id}]}
           chat-props)))

(defn add-chat
  ([cofx chat-id]
   (add-chat cofx chat-id {}))
  ([{:keys [db] :as cofx} chat-id chat-props]
   (let [new-chat       (create-new-chat cofx chat-id chat-props)
         existing-chats (:chats db)]
     {:db        (cond-> db
                         (not (contains? existing-chats chat-id))
                         (update :chats assoc chat-id new-chat))
      :save-chat new-chat})))

;; TODO (yenda): there should be an option to update the timestamp
;; this shouldn't need a specific function like `upsert-chat` which
;; is wrongfuly named
(defn update-chat
  "Updates chat properties, if chat is not present in db, creates a default new one"
  [{:keys [db get-stored-chat] :as cofx} {:keys [chat-id] :as chat}]
  (let [chat (merge (or (get-stored-chat chat-id)
                        (create-new-chat cofx chat-id {}))
                    chat)]
    {:db        (cond-> db
                  (:is-active chat) (update-in [:chats chat-id] merge chat))
     :save-chat chat}))

;; TODO (yenda): an upsert is suppose to add the entry if it doesn't
;; exist and update it if it does
(defn upsert-chat
  "Just like `update-chat` only implicitely updates timestamp"
  [cofx chat]
  (update-chat cofx (assoc chat :timestamp (:now cofx))))
