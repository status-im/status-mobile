(ns status-im.chat.models
  (:require [status-im.ui.components.styles :as styles]))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn toggle-chat-ui-prop
  "Toggles chat ui prop in active chat"
  [{:keys [current-chat-id] :as db} ui-element]
  (update-in db [:chat-ui-props current-chat-id ui-element] not))

(defn- create-new-chat
  [{:keys [db gfy-generator now]} chat-id chat-props]
  (let [{:keys [name whisper-identity]} (get-in db [:contacts/contacts chat-id])]
    (merge {:chat-id    chat-id
            :name       (or name (gfy-generator whisper-identity))
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
     {:db (cond-> db
            (not (contains? existing-chats chat-id))
            (update :chats assoc chat-id new-chat))
      :save-chat new-chat})))

(defn update-chat
  "Updates chat properties, if chat is not present in db, creates a default new one"
  [{:keys [db get-stored-chat]} {:keys [chat-id] :as chat}]
  (let [chat (merge (or (get-stored-chat chat-id)
                        (create-new-chat db chat-id {}))
                    chat)]
    {:db        (update-in db [:chats chat-id] merge chat)
     :save-chat chat}))

(defn upsert-chat
  "Just like `update-chat` only implicitely updates timestamp"
  [cofx chat]
  (update-chat cofx (assoc chat :timestamp (:now cofx))))
