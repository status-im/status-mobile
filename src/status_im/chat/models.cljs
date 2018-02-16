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
  [{:keys [db now]} chat-id]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id    chat-id
     :name       (or name (gfycat/generate-gfy chat-id))
     :color      styles/default-chat-color
     :group-chat false
     :is-active  true
     :timestamp  now
     :contacts   [{:identity chat-id}]}))

(defn add-chat
  "Adds new chat to db & realm, if the chat with same id already exists, justs restores it"
  ([cofx chat-id]
   (add-chat cofx chat-id {}))
  ([{:keys [db get-stored-chat] :as cofx} chat-id chat-props]
   (let [{:keys [deleted-chats]} db
         new-chat (merge (if (get deleted-chats chat-id)
                           (assoc (get-stored-chat chat-id) :is-active true)
                           (create-new-chat cofx chat-id))
                         chat-props)]
     {:db        (-> db
                     (update :chats assoc chat-id new-chat)
                     (update :deleted-chats (fnil disj #{}) chat-id))
      :save-chat new-chat})))

;; TODO (yenda): there should be an option to update the timestamp
;; this shouldn't need a specific function like `upsert-chat` which
;; is wrongfuly named
(defn update-chat
  "Updates chat properties when not deleted, if chat is not present in app-db, creates a default new one"
  [{:keys [db] :as cofx} {:keys [chat-id] :as chat-props}]
  (let [{:keys [chats deleted-chats]} db]
    (if (get deleted-chats chat-id) ;; when chat is deleted, don't change anything
      {:db db}
      (let [chat (merge (or (get chats chat-id)
                            (create-new-chat cofx chat-id))
                        chat-props)]
        {:db        (update-in db [:chats chat-id] merge chat)
         :save-chat chat}))))

;; TODO (yenda): an upsert is suppose to add the entry if it doesn't
;; exist and update it if it does
(defn upsert-chat
  "Just like `update-chat` only implicitely updates timestamp"
  [cofx chat]
  (update-chat cofx (assoc chat :timestamp (:now cofx))))

(defn new-update? [{:keys [added-to-at removed-at removed-from-at]} timestamp]
  (and (> timestamp added-to-at)
       (> timestamp removed-at)
       (> timestamp removed-from-at)))

(defn remove-chat [{:keys [db]} chat-id]
  (let [{:keys [chat-id group-chat debug?]} (get-in db [:chats chat-id])]
    (cond-> {:db                      (-> db
                                          (update :chats dissoc chat-id)
                                          (update :deleted-chats (fnil conj #{}) chat-id))
             :delete-pending-messages chat-id}
            (or group-chat debug?)
            (assoc :delete-messages chat-id)
            debug?
            (assoc :delete-chat chat-id)
            (not debug?)
            (assoc :deactivate-chat chat-id))))
